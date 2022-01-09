package study.datajpa.entity;

import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;


// auditing을 spring data jpa로 사용하는코드 application에 @EnableJpaAuditing 설정필요
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity extends BaseTimeEntity{

    //작성자 수정자는 application에 Bean등록 필요
    //작성자
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    //수정자
    @LastModifiedBy
    private String lastModifiedBy;
}
