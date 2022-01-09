package study.datajpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.repository.MemberRepository;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberRepository memberRepository;

    @Test
    public void testEntity() throws Exception {
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        Member member1 = new Member("member1", 20, teamA);
        Member member2 = new Member("member2", 30, teamA);
        Member member3 = new Member("member3", 50, teamB);
        Member member4 = new Member("member4", 60, teamB);


        //when
        em.persist(teamA);
        em.persist(teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        //초기화
        em.flush(); // 강제로 쿼리 날리게 함
        em.clear(); // 쿼리를 날리고 jpa 영속성 컨텍스트에 있는 캐시를 다 날려버림


        //then
        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("-> member.team = " + member.getTeam());
        }

    }

    @Test
    public void JpaEventBaseEntity() throws Exception {
        //given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1); // 이때 @Prepersist

        Thread.sleep(100);
        member1.setUsername("member2");

        em.flush(); //@PreUpdate
        em.clear();

        //when
        Member member = memberRepository.findById(member1.getId()).get();

        //then
        System.out.println("member.getCreatedDate = " + member.getCreatedDate());
        System.out.println("member.getUpdatedDate = " + member.getLastModifiedDate());
        System.out.println("member.getCreatedBy = " + member.getCreatedBy());
        System.out.println("member.getLastModifiedBy = " + member.getLastModifiedBy());
    }

}