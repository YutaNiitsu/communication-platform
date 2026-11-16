package com.example.app.service;

import com.example.app.domain.operationLog.OperationLogEntity;
import com.example.app.domain.operationLog.OperationLogMapper;
import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.user.UserEntity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

class OperationLogServiceTest {

    @Test
    @DisplayName("1: 正常 - ログ記録成功")
    void testRecord_normal() throws Exception {

        OperationLogMapper mapper = mock(OperationLogMapper.class);
        OperationLogService service = new OperationLogService(mapper);

        UserEntity user = new UserEntity();
        user.setId(10L);

        List<String> target = List.of("message_id=100");
        List<String> result = List.of("ok");

        service.record(user, OperationType.message_created, target, result);

        // insert が呼ばれたことを確認
        ArgumentCaptor<OperationLogEntity> captor = ArgumentCaptor.forClass(OperationLogEntity.class);
        verify(mapper).insert(captor.capture());

        OperationLogEntity saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(10L);
        assertThat(saved.getOperationType()).isEqualTo(OperationType.message_created);
        assertThat(saved.getTarget()).contains("message_id=100");
        assertThat(saved.getResult()).contains("ok");
    }

    @Test
    @DisplayName("2: 異常 - user=null → catch によりアプリは落ちない")
    void testRecord_userNull() {

        OperationLogMapper mapper = mock(OperationLogMapper.class);
        OperationLogService service = new OperationLogService(mapper);

        // user=null
        UserEntity user = null;

        List<String> target = List.of("message_id=100");
        List<String> result = List.of("ok");

        // 例外が外へ出ないことを確認
        assertThatCode(() -> service.record(user, OperationType.message_created, target, result))
                .doesNotThrowAnyException();

        // DB 保存は行われない
        verify(mapper, never()).insert(any());
    }
}
