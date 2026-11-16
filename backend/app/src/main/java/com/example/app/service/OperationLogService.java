package com.example.app.service;

import com.example.app.domain.operationLog.OperationLogEntity;
import com.example.app.domain.operationLog.OperationLogMapper;
import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.user.UserEntity;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");
    private static final Logger operationLog = LoggerFactory.getLogger("OPERATION_LOG");

    /**
     * 操作ログを記録する共通メソッド
     *
     * @param user         操作したユーザー
     * @param type         操作種別（Enum）
     * @param target       操作対象（message_id / channel_id / keyword など）
     * @param result  検索結果
     */
    public void record(UserEntity user, OperationType type, List<String> target, List<String> result) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            OperationLogEntity logEntity = new OperationLogEntity();
            logEntity.setUserId(user.getId());
            logEntity.setOperationType(type);

            logEntity.setTarget(target != null ? mapper.writeValueAsString(target) : null);
            logEntity.setResult(result != null ? mapper.writeValueAsString(result) : null);

            // DBに保存
            operationLogMapper.insert(logEntity);
            // ログ出力
            operationLog.info("{userId={}, type={}, target={}, result={}}",
                    user.getId(), type, logEntity.getTarget(), logEntity.getResult());

        } catch (Exception e) {
            appLog.error("Failed to save log: {}", e.getMessage());
        }
    }


}
