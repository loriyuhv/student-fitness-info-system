package com.wsw.fitnesssystem.user.application.port.output;

import com.wsw.fitnesssystem.user.application.dto.command.UserAccountProvisionCommand;
import com.wsw.fitnesssystem.user.application.dto.result.UserAccountProvisionResult;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/16 11:59
 * @since 1.0
 */
public interface UserAccountProvisioningPort {

    UserAccountProvisionResult createAccount(UserAccountProvisionCommand command);

}
