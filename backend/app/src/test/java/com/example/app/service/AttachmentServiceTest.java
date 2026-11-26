package com.example.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.example.app.config.AppProperties;
import com.example.app.domain.attachment.AttachmentMapper;
import com.example.app.domain.message.MessageEntity;
import com.example.app.domain.message.MessageMapper;
import com.example.app.domain.user.UserEntity;
import com.example.app.exception.NotFoundException;
import com.example.app.security.AuthUtil;
import com.example.app.websocket.WebSocketEventPublisher;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @InjectMocks
    private AttachmentService attachmentService;

    @Mock
    private AppProperties props;

    @Mock
    private AppProperties.Attachment attachmentProps;

    @Mock
    private AppProperties.Storage storageProps;

    @Mock
    private StorageUsageChecker usageChecker;

    @Mock
    private LocalFileStorage fileStorage;

    @Mock
    private ThumbnailGenerator thumbnailGenerator;

    @Mock
    private AttachmentMapper attachmentMapper;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private WebSocketEventPublisher eventPublisher;

    @Mock
    private FileNameGenerator fileNameGenerator;

    @Mock
    private OperationLogService operationLogService;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private HttpServletRequest request;

    private UserEntity user;
    private MessageEntity message;

    @BeforeEach
    void setup() {

        user = new UserEntity();
        user.setId(1L);

        message = new MessageEntity();
        message.setId(1L);
        message.setChannelId(1L);
        message.setUserId(1L);

        when(authUtil.getTokenFromCookie(any()))
                .thenReturn("token");

        when(authUtil.getUserFromToken(any()))
                .thenReturn(user);

        when(props.getStorage())
                .thenReturn(storageProps);

        when(storageProps.getDeleteThresholdPercent())
                .thenReturn(95);
    }

    private void setupMessageExists() {

        when(messageMapper.findById(anyLong()))
                .thenReturn(Optional.of(message));
    }

    private void setupStorageValidation() {

        setupMessageExists();

        when(usageChecker.getUsagePercent())
                .thenReturn(50);
    }

    private void setupExtensionValidation() {

        setupStorageValidation();

        when(props.getAttachment())
                .thenReturn(attachmentProps);

        when(attachmentProps.getAllowedExtensions())
                .thenReturn(List.of(
                        "pdf",
                        "png"
                ));
    }

    private void setupMimeValidation() {

        setupExtensionValidation();

        when(attachmentProps.getAllowedMimeTypes())
                .thenReturn(List.of(
                        "application/pdf",
                        "image/png"
                ));
    }

    private void setupSizeValidation() {

        setupMimeValidation();

        when(attachmentProps.getMaxFileSizeMb())
                .thenReturn(30L);
    }

    private void setupUploadSuccess() {

        setupSizeValidation();

        when(fileNameGenerator.generate(any()))
                .thenReturn("stored-file.pdf");

        when(fileStorage.save(any(), any()))
                .thenReturn("/files/stored-file.pdf");

        doNothing().when(attachmentMapper)
                .insert(any());

        doNothing().when(eventPublisher)
                .publishAttachmentAdded(
                        anyLong(),
                        any()
                );

        doNothing().when(operationLogService)
                .record(
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void ストレージ使用率が閾値未満の場合_アップロード成功() {

        setupUploadSuccess();

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.pdf",
                        "application/pdf",
                        "hello".getBytes()
                );

        assertDoesNotThrow(() ->
                attachmentService.uploadAttachment(
                        1L,
                        file,
                        "test.pdf",
                        request
                )
        );
    }

    @Test
    void ストレージ使用率が削除閾値以上の場合_例外() {

        setupMessageExists();

        when(usageChecker.getUsagePercent())
                .thenReturn(95);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.pdf",
                        "application/pdf",
                        "hello".getBytes()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> attachmentService.uploadAttachment(
                        1L,
                        file,
                        "test.pdf",
                        request
                )
        );
    }

    @Test
    void 許可拡張子の場合_成功() {

        setupUploadSuccess();

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "sample.pdf",
                        "application/pdf",
                        "data".getBytes()
                );

        assertDoesNotThrow(() ->
                attachmentService.uploadAttachment(
                        1L,
                        file,
                        "test.pdf",
                        request
                )
        );
    }

    @Test
    void 禁止拡張子の場合_例外() {

        setupExtensionValidation();

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "sample.exe",
                        "application/octet-stream",
                        "data".getBytes()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> attachmentService.uploadAttachment(
                        1L,
                        file,
                        "test.pdf",
                        request
                )
        );
    }

    @Test
    void 禁止MimeTypeの場合_例外() {

        setupMimeValidation();

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "sample.pdf",
                        "application/octet-stream",
                        "data".getBytes()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> attachmentService.uploadAttachment(
                        1L,
                        file,
                        "test.pdf",
                        request
                )
        );
    }

    @Test
    void ファイルサイズ超過の場合_例外() {

        setupSizeValidation();

        byte[] bytes =
                new byte[31 * 1024 * 1024];

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "sample.pdf",
                        "application/pdf",
                        bytes
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> attachmentService.uploadAttachment(
                        1L,
                        file,
                        "test.pdf",
                        request
                )
        );
    }

    @Test
    void 拡張子なしファイルの場合_例外() {

        setupStorageValidation();

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "sample",
                        "application/pdf",
                        "data".getBytes()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> attachmentService.uploadAttachment(
                        1L,
                        file,
                        "test.pdf",
                        request
                )
        );
    }

    @Test
    void tarGzファイルの場合_成功() {

        setupUploadSuccess();

        when(attachmentProps.getAllowedExtensions())
                .thenReturn(List.of("tar.gz"));

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "sample.tar.gz",
                        "application/gzip",
                        "data".getBytes()
                );

        when(attachmentProps.getAllowedMimeTypes())
                .thenReturn(List.of("application/gzip"));

        assertDoesNotThrow(() ->
                attachmentService.uploadAttachment(
                        1L,
                        file,
                        "test.pdf",
                        request
                )
        );
    }

    @Test
    void ファイルサイズ境界値_30MBちょうどは正常() {

        setupUploadSuccess();

        byte[] bytes =
                new byte[30 * 1024 * 1024];

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "sample.pdf",
                        "application/pdf",
                        bytes
                );

        assertDoesNotThrow(() ->
                attachmentService.uploadAttachment(
                        1L,
                        file,
                        "test.pdf",
                        request
                )
        );
    }

    
}