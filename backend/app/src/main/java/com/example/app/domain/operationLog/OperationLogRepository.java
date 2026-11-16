// package com.example.app.domain.operationLog;

// import com.example.app.domain.user.UserEntity;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.List;

// public interface OperationLogRepository extends JpaRepository<OperationLogEntity, Long> {

//     // ユーザー別ログ一覧
//     List<OperationLogEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

//     // 操作種別別ログ一覧
//     List<OperationLogEntity> findByOperationTypeOrderByCreatedAtDesc(OperationType operationType);

//     // 操作種別 + ユーザー別ログ一覧
//     List<OperationLogEntity> findByUserAndOperationTypeOrderByCreatedAtDesc(
//             UserEntity user,
//             OperationType operationType
//     );
// }
