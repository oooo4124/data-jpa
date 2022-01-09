package study.datajpa.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class TeamJpaRepository {

    public final EntityManager em;

    public Team save(Team team) {
        em.persist(team);
        return team;
    }

    public void delete(Team team) {
        em.remove(team);
    }

    public List<Team> findAll() {
        return em.createQuery("select t from Team t", Team.class)
                .getResultList();
    }

    public Optional<Team> findById(Long id) {
        Team team = em.find(Team.class, id);
        return Optional.ofNullable(team); // 널일수도 있고 아닐수도 있다.
    }

    public long count() {
        return em.createQuery("select count(t) from Team t", Long.class)
                .getSingleResult(); // getSingleResult() 하나만 반환할때
    }

    public Team find(Long id) {
        return em.find(Team.class, id);
    }
}
