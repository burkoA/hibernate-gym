package epam.arsen.burko.gym.repository;

import epam.arsen.burko.gym.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUsernameStartingWith(String baseUsername);
    Optional<User> findByUsername(String username);
}
