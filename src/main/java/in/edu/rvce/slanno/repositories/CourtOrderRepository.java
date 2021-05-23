package in.edu.rvce.slanno.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import in.edu.rvce.slanno.entities.CourtOrder;

@Repository
public interface CourtOrderRepository extends PagingAndSortingRepository<CourtOrder, String> {

}
