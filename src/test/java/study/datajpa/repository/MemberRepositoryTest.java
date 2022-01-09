package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository; // 구현체는 spring data jpa가 만들어서 주입해준다.
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    EntityManager em;

    @Test
    public void save() throws Exception {
        //given
        Team team = new Team("teamA");
        Member member = new Member("test",20, team);
        member.setUsername("memberA");
        
        //when
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get(); // 이렇게 .get() 쓰면 안됨 좋은 방법 아님 null일때 처리하려면 orElse 써서 값 없을때 할 행동 정해줘야함

        //then
        assertThat(findMember).isEqualTo(member);
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());


    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //list 조회
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //count 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);

    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("aaa", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("aaa", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("aaa");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void NamedQuery() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("aaa");

        Member findMember = result.get(0);

        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    public void testQuery() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("aaa",10);

        Member findMember = result.get(0);

        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    public void findUsernameList() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> result = memberRepository.findUsernameList();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto() {

        Team t1 = new Team("a");
        teamRepository.save(t1);

        Member m1 = new Member("aaa", 10);
        m1.setTeam(t1);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();

        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames() {

        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> Names = memberRepository.findByNames(Arrays.asList("aaa", "bbb"));

        for (Member name : Names) {
            System.out.println("name = " + name);
        }
    }

    @Test
    public void returnType() {

        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> aaa = memberRepository.findListByUsername("aaa"); // 컬렉션의 경우 값이 없을 때 null이 아님 emptyCollection을 반환해줌
        Member aaa1 = memberRepository.findMember1ByUsername("aaa"); // 값이 없으면 null이 반환됨
        Optional<Member> aaa2 = memberRepository.findOptionalByUsername("aaa"); // 값이 없으면 optional.empty반환 orelse등으로 처리한다. 값 두개이상이면 예외 터짐

    }

    @Test
    public void paging() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        // 페이지는 0부터시작 0페이지에서 3개 가져와서 username DESC로 정렬한다
        // Pagable 인터페이스 구현체 주로 PageRequest를 사용한다.
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        //실무 꿀팁 page를 api에서 반환하면 안됨 Member 엔티티이기 때문에 dto 변환해야함 다음은 쉽게 변환하는 방법
        Page<MemberDto> tomap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));// 이렇게 바꿔서 반환

//        Slice<Member> page1 = memberRepository.findByAge(age, pageRequest); // slice는 3개 요청하면 +1 해서 4개를 요청한다



        //then
        List<Member> content = page.getContent(); // 페이지 데이터 꺼내기
        long totalElements = page.getTotalElements(); // totalCount랑 같음

//        List<Member> sliceContent = page1.getContent(); // 페이지 데이터 꺼내기


//        //Page
        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0); // 페이지 가져오기
        assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 갯수
        assertThat(page.isFirst()).isTrue(); // 첫 페이지 인지
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는지

        //Slice 토탈카운트,토탈페이지 가져오는 기능 없음
//        assertThat(sliceContent.size()).isEqualTo(3);
//        assertThat(page1.getNumber()).isEqualTo(0); // 페이지 가져오기
//        assertThat(page1.isFirst()).isTrue(); // 첫 페이지 인지
//        assertThat(page1.hasNext()).isTrue(); // 다음 페이지가 있는지
        
    }

    @Test
    public void bulkUpdate() {
        //given
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",19));
        memberRepository.save(new Member("member3",20));
        memberRepository.save(new Member("member4",21));
        memberRepository.save(new Member("member5",40));

        //when
        int resultCount = memberRepository.bulkAgeplus(20);
        // 또는 modtfying 옵션설정
//        em.flush(); // 남아있는 변경되지 않은 점들이 db에 반영
//        em.clear(); // 영속성 컨텍스트 날려버림

        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);

        //then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {
        //given
        //member1 -> teamA
        //member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 데이터베이스 반영 후 영속성 컨텍스트 날림
        em.flush();
        em.clear();

        //when  N+1 문제
        //select Member 멤버만 가져옴
        List<Member> members = memberRepository.findAll();
        
        //fetch join으로 해결
        List<Member> memberFetchJoin = memberRepository.findMemberFetchJoin();

        //@EntityGraph 사용
        List<Member> egMembers = memberRepository.findAll();

        for (Member member : egMembers) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            //select Team 이때 team 가져옴(프록시 초기화) -> 두 번 조회하게 됨(members일때 문제 -> memberFetchJoin일때 해결)
            System.out.println("member.team = " + member.getTeam().getName());

        }

    }

    @Test
    public void queryHint() {
        //given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush(); // 트랜잭션 커밋될 때 자동으로 플러시되는데 이것은 강제로 플러시
        em.clear();

        //when
        Member findMember = memberRepository.findById(member1.getId()).get();
        findMember.setUsername("member2"); // 이때는 쿼리가 나가지 않음

        //쿼리 힌트 사용
        Member readOnlyByUsername = memberRepository.findReadOnlyByUsername("member1");
        readOnlyByUsername.setUsername("member2");

        // 변경 감지 동작되어 db에 update 쿼리가 나감
        //단점 : 원본 객체가 있어야함 , 객체를 두개 관리해야한다 비효율적
        em.flush();
    }

    @Test
    public void lock() {

        //given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
        List<Member> result = memberRepository.findLockByUsername("member1");


    }

    //사용자 정의 리포지토리 구현
    @Test
    public void callCustom() {
        memberRepository.findMemberCustom();
    }


    //projections
    @Test
    public void projections() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        em.persist(member1);
        em.persist(member2);

        em.flush();
        em.clear();

        List<UsernameOnlyDto> result = memberRepository.findProjectionsByUsername("member1");

        for (UsernameOnlyDto usernameOnly : result) {
            System.out.println("usernameOnly = " + usernameOnly.getUsername());
        }

    }

    //네이티브 쿼리 테스트
    @Test
    public void nativeQuery() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        em.persist(member1);
        em.persist(member2);

        em.flush();
        em.clear();

        Member result = memberRepository.findByNativeQuery("member1");
        System.out.println("result = " + result);

    }

    //네이티브 쿼리 + 프로젝션 테스트
    @Test
    public void nativeProjectionQuery() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        em.persist(member1);
        em.persist(member2);

        em.flush();
        em.clear();

        Page<MemberProjection> result = memberRepository.findByNativeProjection(PageRequest.of(0, 10));
        List<MemberProjection> content = result.getContent();
        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection = " + memberProjection.getUsername());
            System.out.println("memberProjection = " + memberProjection.getTeamName());
        }

    }
}