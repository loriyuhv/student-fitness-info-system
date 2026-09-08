package com.wsw.fitnesssystem.user.infrastructure.persistence.converter;

import com.wsw.fitnesssystem.user.domain.model.StudentProfile;
import com.wsw.fitnesssystem.user.domain.valueobject.Gender;
import com.wsw.fitnesssystem.user.domain.valueobject.Status;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.StudentProfilePo;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 09:05
 * @since 1.0
 */
@Component
public class StudentProfileConverter {

    public StudentProfilePo toPo(StudentProfile profile) {
        StudentProfilePo po = new StudentProfilePo();
        po.setStudentId(profile.getStudentId());
        po.setCampusId(profile.getCampusId());
        po.setUserId(profile.getUserId());
        po.setStudentNo(profile.getStudentNo());
        po.setClassId(profile.getClassId());
        po.setEnrollYear(profile.getEnrollYear());
        po.setMajor(profile.getMajor());
        po.setIdCard(profile.getIdCard());
        po.setGender(profile.getGender().getCode());
        po.setBirthDate(profile.getBirthDate());
        po.setFamilyAddress(profile.getFamilyAddress());
        po.setAvatarUrl(profile.getAvatarUrl());
        po.setStatus(profile.getStatus().getCode());
        po.setDeleted(profile.isDeleted() ? 1 : 0);
        po.setRemark(profile.getRemark());
        return po;
    }

    public StudentProfile toDomain(StudentProfilePo po) {
        return StudentProfile.builder()
            .studentId(po.getStudentId())
            .campusId(po.getCampusId())
            .userId(po.getUserId())
            .studentNo(po.getStudentNo())
            .classId(po.getClassId())
            .enrollYear(po.getEnrollYear())
            .major(po.getMajor())
            .idCard(po.getIdCard())
            .gender(Gender.of(po.getGender()))
            .birthDate(po.getBirthDate())
            .familyAddress(po.getFamilyAddress())
            .avatarUrl(po.getAvatarUrl())
            .status(Status.of(po.getStatus()))
            .deleted(po.getDeleted() == 1)
            .remark(po.getRemark())
            .build();
    }

}
