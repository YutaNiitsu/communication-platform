package com.example.app.domain.attachment;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AttachmentMapper {

    @Insert("""
        INSERT INTO message_attachments (
            message_id,
            original_file_name,
            stored_file_name,
            file_url,
            thumbnail_url,
            file_type,
            file_size,
            created_at
        ) VALUES (
            #{messageId},
            #{originalFileName},
            #{storedFileName},
            #{fileUrl},
            #{thumbnailUrl},
            #{fileType},
            #{fileSize},
            #{createdAt}
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AttachmentEntity attachment);


    @Select("""
        SELECT *
        FROM message_attachments
        WHERE id = #{id}
    """)
    @Results(id = "AttachmentResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "messageId", column = "message_id"),
            @Result(property = "originalFileName", column = "original_file_name"),
            @Result(property = "storedFileName", column = "stored_file_name"),
            @Result(property = "fileUrl", column = "file_url"),
            @Result(property = "thumbnailUrl", column = "thumbnail_url"),
            @Result(property = "fileType", column = "file_type"),
            @Result(property = "mimeType", column = "mime_type"),
            @Result(property = "extension", column = "extension"),
            @Result(property = "fileSize", column = "file_size"),
            @Result(property = "createdAt", column = "created_at")
    })
    Optional<AttachmentEntity> findById(Long id);


    @Select("""
        SELECT *
        FROM message_attachments
        ORDER BY created_at ASC
        LIMIT #{limit}
    """)
    @ResultMap("AttachmentResultMap")
    List<AttachmentEntity> findOldest(int limit);


    @Select("""
        SELECT *
        FROM message_attachments
        ORDER BY file_size DESC
        LIMIT #{limit}
    """)
    @ResultMap("AttachmentResultMap")
    List<AttachmentEntity> findLargest(int limit);


    @Delete("""
        DELETE FROM message_attachments
        WHERE id = #{id}
    """)
    void delete(Long id);

    @Select("""
        SELECT *
        FROM message_attachments
        ORDER BY created_at ASC
    """)
    @ResultMap("AttachmentResultMap")
    List<AttachmentEntity> findOrderByCreatedAtAsc();

    @Select("""
        SELECT *
        FROM message_attachments
        WHERE message_id = #{messageId}
        ORDER BY created_at ASC
    """)
    @ResultMap("AttachmentResultMap")
    List<AttachmentEntity> findByMessageId(Long messageId);

}
