package com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.assembler;

import com.wsw.fitnesssystem.iam.authentication.application.dto.command.LogoutCommand;
import com.wsw.fitnesssystem.shared.application.context.RequestContextHolder;
import com.wsw.fitnesssystem.shared.domain.vb.Operator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/21 13:01
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class LogoutWebAssembler {

    public LogoutCommand toCommand() {
        Operator operator = RequestContextHolder.getRequiredOperator();
        String accessTokenId = RequestContextHolder.getTokenId();
        return new LogoutCommand(
            operator.userId(),
            operator.campusId(),
            accessTokenId
        );
    }

}
