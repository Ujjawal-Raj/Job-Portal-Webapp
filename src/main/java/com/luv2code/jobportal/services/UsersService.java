package com.luv2code.jobportal.services;

import com.luv2code.jobportal.entity.JobSeekerProfile;
import com.luv2code.jobportal.entity.RecruiterProfile;
import com.luv2code.jobportal.entity.Users;
import com.luv2code.jobportal.repository.JobSeekerProfileRepository;
import com.luv2code.jobportal.repository.RecruiterProfileRepository;
import com.luv2code.jobportal.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersService(UsersRepository usersRepository,
                        RecruiterProfileRepository recruiterProfileRepository,
                        JobSeekerProfileRepository jobSeekerProfileRepository,
                        PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Users addNew(Users users) {
        users.setActive(true);
        users.setRegistrationDate(new Date(System.currentTimeMillis()));
        users.setPassword(passwordEncoder.encode(users.getPassword()));

        Users savedUsers = usersRepository.save(users);

        int userTypeId = users.getUserTypeId().getUserTypeId();

        if(userTypeId == 1)
            recruiterProfileRepository.save(new RecruiterProfile(savedUsers));
        else
            jobSeekerProfileRepository.save(new JobSeekerProfile(savedUsers));

        return savedUsers;
    }

    public Optional<Users> getUserByAnEmail(String email) {
        return usersRepository.findByEmail(email);
    }

    public Object getCurrentUserProfile() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(!(authentication instanceof AnonymousAuthenticationToken)){
            String userName = authentication.getName();
            Users users = usersRepository.findByEmail(userName).orElseThrow(() -> new UsernameNotFoundException("Could " +
                    "not found user"));
            int userId = users.getUserId();
            if(authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))) {
                return recruiterProfileRepository.findById(userId)
                        .orElse(new RecruiterProfile());
            } else {
                return jobSeekerProfileRepository.findById(userId)
                        .orElse(new JobSeekerProfile());
            }
        }
        return null;

    }

    public Users getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)) {
            String userName = authentication.getName();
            Users user = usersRepository.findByEmail(userName).orElseThrow(() -> new UsernameNotFoundException("Could " +
                    "not found user"));
            return user;
        }
        return null;

    }

    public Users findByEmail(String currentUserName) {

        return usersRepository.findByEmail(currentUserName).orElseThrow(() -> new UsernameNotFoundException("Could " +
                "not found the user"));

    }
}
