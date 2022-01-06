package in.edu.rvce.slanno.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import in.edu.rvce.slanno.entities.SystemSetting;

@Repository
public interface SystemSettingRepository extends PagingAndSortingRepository<SystemSetting, Long> {

}