package in.edu.rvce.slanno.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import in.edu.rvce.slanno.entities.Authorities;

@Repository
public interface AuthoritiesRepository extends PagingAndSortingRepository<Authorities, String> {

}