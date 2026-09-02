package com.wsw.fitnesssystem.user.infrastructure.persistence.converter;

import com.wsw.fitnesssystem.user.domain.model.TeacherProfile;
import com.wsw.fitnesssystem.user.domain.valueobject.Gender;
import com.wsw.fitnesssystem.user.domain.valueobject.Status;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.TeacherProfilePo;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 09:06
 * @since 1.0
 */
@Component
public class TeacherProfileConverter {

    public TeacherProfilePo toPo(TeacherProfile profile) {
        TeacherProfilePo po = new TeacherProfilePo();
        po.setTeacherId(profile.getTeacherId());
        po.setCampusId(profile.getCampusId());
        po.setUserId(profile.getUserId());
        po.setTeacherNo(profile.getTeacherNo());
        po.setGender(profile.getGender().getCode());
        po.setStatus(profile.getStatus().getCode());
        po.setDeleted(profile.isDeleted() ? 1 : 0);
        po.setRemark(profile.getRemark());
        return po;
    }

    public TeacherProfile toDomain(TeacherProfilePo po) {
        return TeacherProfile.builder()
            .teacherId(po.getTeacherId())
            .campusId(po.getCampusId())
            .userId(po.getUserId())
            .teacherNo(po.getTeacherNo())
            .gender(Gender.of(po.getGender()))
            .status(Status.of(po.getStatus()))
            .deleted(po.getDeleted() == 1)
            .remark(po.getRemark())
            .build();
    }

}
