package eternal_return.dayogg.route_auth.repository;

import eternal_return.dayogg.route_auth.RouteAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteAuthRepository extends JpaRepository<RouteAuth, Long> {
}
