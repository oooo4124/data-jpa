package study.datajpa.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByUsernameAndAgeGreaterThan(String userName, int age);

//    @Query(name = "Member.findByUsername") //주석처리해도 동작 순서는 1. named 쿼리 찾기 2. 메소드명으로 쿼리 생성
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username = :username and m.age = :age")//
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    // 값 조회
    @Query("select m.username from Member m") 
    List<String> findUsernameList();

    //DTO로 조회(new operation 사용해야함 jpql이 제공하는 문법)
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    //collection타입 in절 조회 (실무에서 많이 사용)
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    //다양한 반환타입
    List<Member> findListByUsername(String username); //컬렉션
    Member findMember1ByUsername(String username); // 단건
    Optional<Member> findOptionalByUsername(String username); // 단건에 Optional

    //spring data jpa 페이징과 정렬
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m) from Member m")// count 쿼리 분리 / sort의 경우도 복잡해지면 sort.by 지우고 여기에 추가하면됨
    Page<Member> findByAge(int age, Pageable pageable); // count query사용 (count 쿼리 분리 annotation 설정 안해줄경우)
//    Slice<Member> findByAge(int age, Pageable pageable); // slice사용
//    List<Member> findByAge(int age, Pageable pageable); // List로 받기도 가능 제한된 데이터만 보고싶을때

    //spring data jpa bulk update
    @Modifying(clearAutomatically = true) // 이게 있어야 executeUpdate 실행 / clearAutomatically은 쿼리 나간 후 영속성 컨텍스트 자동으로 clear
    @Query("Update Member m set m.age = m.age + 1 where m.age >=:age")
    int bulkAgeplus(@Param("age") int age);

    //N+1 문제 해결 Fetch join
    @Query("select m from Member m left join fetch m.team")// member를 조회할 때 연관된 team을 한번에 다 불러옴
    List<Member> findMemberFetchJoin();

    //spring data jpa의 @EntityGraph -> 메서드 이름으로 쿼리 날리면서 fetch join을 쓰는 방법
    @Override
    @EntityGraph(attributePaths = {"team"}) // member와 team을 같이 조회하면서 jpql을 짜지 않는 방법
    List<Member> findAll();

    //sqpl 짰는데 fetch join만 추가하고 싶을 경우
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    //메소드 이름 사용하면서 entitygraph 사용
//    @EntityGraph(attributePaths = {"team"})
    @EntityGraph("Member.all") // @NamedEntityGraph 쓸 경우
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    //만들때 스냅샷을 만들지 않음 변경이 되지 않을 것이기 때문 변경이 일어나면 무시해버림 읽기로 최적화
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    //락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);

    //projections
    List<UsernameOnlyDto> findProjectionsByUsername(@Param("username") String username);

    //네이티브 쿼리
    @Query(value = "select * from member where username=?", nativeQuery = true)
    Member findByNativeQuery(String username);

    //네이티브 쿼리 + 프로젝션 - 페이징 가능
    @Query(value = "select m.member_id as id, m.username, t.name as teamName" +
            " from member m left join team t",
            countQuery = "select count(*) from member"
            ,nativeQuery = true)
    Page<MemberProjection> findByNativeProjection(Pageable pageable);

}
