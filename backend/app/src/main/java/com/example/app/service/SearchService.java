package com.example.app.service;

import com.example.app.domain.message.MessageMapper;
import com.example.app.domain.operationLog.OperationType;
import com.example.app.domain.user.UserEntity;
import com.example.app.dto.message.MessageDTO;
import com.example.app.dto.search.SearchRequestDTO;
import com.example.app.dto.search.SearchResponseDTO;
import com.example.app.exception.BadRequestException;
import com.example.app.exception.NotFoundException;
import com.example.app.security.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
	private final JwtProvider jwtProvider;
    private final OperationLogService operationLogService;
	private final MessageMapper messageMapper;
	private final AuthUtil authUtil;
	private static final Logger appLog = LoggerFactory.getLogger("APP_LOG");

    public List<SearchResponseDTO> search(SearchRequestDTO req, HttpServletRequest request) {
		String token = jwtProvider.extractTokenFromCookie(request);
        UserEntity requestUser = authUtil.getUserFromToken(token);

		// 2. 検索条件バリデーション
		if (req == null) {
			appLog.warn("Search request is required");
			throw new BadRequestException("Search request is required");
		}

		if (req.keyword() != null && req.keyword().isBlank()) {
			appLog.warn("Keyword must not be blank");
			throw new BadRequestException("Keyword must not be blank");
		}

		if (req.from() != null && req.to() != null && req.from().isAfter(req.to())) {
			appLog.warn("Invalid date range: from > to");
			throw new BadRequestException("Invalid date range: from > to");
		}

		// 3. LocalDate → LocalDateTime 変換
		LocalDateTime fromDateTime = (req.from() != null) ? req.from().atStartOfDay() : null;
		LocalDateTime toDateTime = (req.to() != null) ? req.to().atTime(LocalTime.MAX) : null;

		// 4. 検索実行
		List<MessageDTO> results = messageMapper.searchMessages(
				req.keyword(),
				req.channelId(),
				req.userId(),
				fromDateTime,
				toDateTime
		);

		// 5. 検索結果が0件の場合の扱い
		if (results.isEmpty()) {
			appLog.warn("No messages found");
			throw new NotFoundException("No messages found");
		}

		// 6. 操作ログ記録
		operationLogService.record(
			requestUser,
			OperationType.search,
			List.of("keyword=" + req.keyword()
					, "channel_id=" + req.channelId()
					, "user_id=" + req.userId()
					, "from=" + req.from()
					, "to=" + req.to()),
			List.of("result_count=" + results.size())
		);


		// 7. DTO 変換
		return results.stream()
				.map(m -> new SearchResponseDTO(
						m.id(),
						m.channelId(),
						m.userId(),
						m.replyToMessageId(),
						m.content(),
						m.status(),
						m.createdAt(),
						m.updatedAt()
				))
				.toList();
	}
}