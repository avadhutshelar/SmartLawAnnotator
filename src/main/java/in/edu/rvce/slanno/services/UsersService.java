package in.edu.rvce.slanno.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.edu.rvce.slanno.entities.Authorities;
import in.edu.rvce.slanno.entities.Users;
import in.edu.rvce.slanno.enums.UserAuthorities;
import in.edu.rvce.slanno.enums.UserDto;
import in.edu.rvce.slanno.enums.UserEnabled;
import in.edu.rvce.slanno.repositories.AuthoritiesRepository;
import in.edu.rvce.slanno.repositories.UsersRepository;

@Service
public class UsersService {

	@Autowired
	private UsersRepository usersRepository;
	
	@Autowired
	private AuthoritiesRepository authoritiesRepository;
	
	
	public List<UserDto> getUserDtos() throws Exception{
		List<UserDto> userDtoList = new ArrayList<>();
		List<Users> usersList = Lists.newArrayList(usersRepository.findAll());
		List<Authorities> authoritiesList = Lists.newArrayList(authoritiesRepository.findAll());
		for(Users user: usersList) {
			for(Authorities authorities:authoritiesList) {
				if(StringUtils.equalsIgnoreCase(user.getUsername(), authorities.getUsername())) {
					UserDto userDto = new UserDto();
					userDto.setUsername(user.getUsername());
					userDto.setPassword(user.getPassword());
					userDto.setEnabled((user.getEnabled())?UserEnabled.YES:UserEnabled.NO);
					UserAuthorities tempAutority=null;
					if(StringUtils.equalsIgnoreCase(authorities.getAuthority(), UserAuthorities.ADMIN.getDisplayValue())) {
						tempAutority=UserAuthorities.ADMIN;
					}else if(StringUtils.equalsIgnoreCase(authorities.getAuthority(), UserAuthorities.ANNOTATOR.getDisplayValue())) {
						tempAutority=UserAuthorities.ANNOTATOR;
					}
					userDto.setAuthority(tempAutority);
					userDtoList.add(userDto);
				}
			}
		}
		return userDtoList;
	}
	
	public UserDto getUserDto(String username) throws Exception{
		
		Users user = usersRepository.findById(username)
			      .orElseThrow(() -> new IllegalArgumentException("Invalid user username:" + username));
		
		Authorities authority = authoritiesRepository.findById(username)
			      .orElseThrow(() -> new IllegalArgumentException("Invalid user username:" + username));
		
		UserDto userDto = new UserDto();
		if(null!=user && null!=authority) {
			userDto.setUsername(user.getUsername());
			userDto.setPassword(user.getPassword());
			userDto.setEnabled((user.getEnabled())?UserEnabled.YES:UserEnabled.NO);
			UserAuthorities tempAutority=null;
			if(StringUtils.equalsIgnoreCase(authority.getAuthority(), UserAuthorities.ADMIN.getDisplayValue())) {
				tempAutority=UserAuthorities.ADMIN;
			}else if(StringUtils.equalsIgnoreCase(authority.getAuthority(), UserAuthorities.ANNOTATOR.getDisplayValue())) {
				tempAutority=UserAuthorities.ANNOTATOR;
			}
			userDto.setAuthority(tempAutority);
		}
		
		return userDto;
	}
	
	public void createOrUpdateUser(UserDto userDto) {
		Users user = new Users();
		user.setUsername(userDto.getUsername());
		user.setPassword(userDto.getPassword());
		
		if(StringUtils.equalsIgnoreCase(userDto.getEnabled().getDisplayValue(), UserEnabled.YES.getDisplayValue())) {
			user.setEnabled(Boolean.TRUE);
		}else if(StringUtils.equalsIgnoreCase(userDto.getEnabled().getDisplayValue(), UserEnabled.NO.getDisplayValue())) {
			user.setEnabled(Boolean.FALSE);
		}
		
		usersRepository.save(user);
		
		Authorities authority = new Authorities();
		authority.setUsername(userDto.getUsername());
		if(StringUtils.equalsIgnoreCase(userDto.getAuthority().getDisplayValue(), UserAuthorities.ADMIN.getDisplayValue())) {
			authority.setAuthority(UserAuthorities.ADMIN.getDisplayValue());
		}else if(StringUtils.equalsIgnoreCase(userDto.getAuthority().getDisplayValue(), UserAuthorities.ANNOTATOR.getDisplayValue())) {
			authority.setAuthority(UserAuthorities.ANNOTATOR.getDisplayValue());
		}
		
		authoritiesRepository.save(authority);
	}
	
	public void deleteUser(String username) {
		
		Authorities authority = authoritiesRepository.findById(username).orElse(null);//TODO-AuthoritiesNotFoundException
		
		authoritiesRepository.delete(authority);
		
		Users user = usersRepository.findById(username).orElse(null); //TODO-UserNotFoundException
		
		usersRepository.delete(user);
		
		
	}
	
}
