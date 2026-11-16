// package com.example.app.domain.messageTemplate;

// import java.util.List;

// import org.springframework.data.jpa.repository.JpaRepository;

// public interface MessageTemplateRepository
//         extends JpaRepository<MessageTemplateEntity, Long> {
//     // user_id
//     List<MessageTemplateEntity> findByUserId(Long userId);

//     // is_shared
//     List<MessageTemplateEntity> findByIsShared(boolean isShared);

//     // category_id
//     List<MessageTemplateEntity> findByCategoryId(Long categoryId);

//     // user_id AND category_id
//     List<MessageTemplateEntity> findByUserIdAndCategoryId(Long userId, Long categoryId);

//     // user_id AND is_shared
//     List<MessageTemplateEntity> findByUserIdAndIsShared(Long userId, boolean isShared);

//     // category_id AND is_shared
//     List<MessageTemplateEntity> findByCategoryIdAndIsShared(Long categoryId, boolean isShared);

//     // user_id AND category_id AND is_shared
//     List<MessageTemplateEntity> findByUserIdAndCategoryIdAndIsShared(Long userId, Long categoryId, boolean isShared);
// }
