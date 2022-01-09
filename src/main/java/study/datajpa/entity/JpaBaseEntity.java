package study.datajpa.entity;

import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 속성들만 내려서 사용 진짜 상속 x
public class JpaBaseEntity {

    @Column(updatable = false) // 값이 바뀌지 않음
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist // 저장하기 전에 실행 최초 등록 데이터
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
    }

    @PreUpdate// 업데이트 전에 실행
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
