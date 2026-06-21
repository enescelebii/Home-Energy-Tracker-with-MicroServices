package com.vena.user_service.service;


import com.vena.user_service.dto.UserDto;
import com.vena.user_service.entity.User;
import com.vena.user_service.exception.UserNotFoundException;
import com.vena.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;


    public UserDto createUser(UserDto input) {
        final User createdUser = User.builder()
                .name(input.getName())
                .surname(input.getSurname())
                .email(input.getEmail())
                .address(input.getAddress())
                .alerting(input.getAlerting())
                .energyAlertingThreshold(input.getEnergyAlertingThreshold())
                .build();

        final User saved = userRepository.save(createdUser);


        return toDto(saved);
    }

    private UserDto toDto(User user){
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .address(user.getAddress())
                .alerting(user.getAlerting())
                .energyAlertingThreshold(user.getEnergyAlertingThreshold())
                .build();
    }


    public UserDto getUserById(Long id) {
        return userRepository.findById(id).map(this::toDto).orElse(null);
    }

    public UserDto updateUser(UserDto userDto) {
        return getUserById(userDto.getId()) != null ? toDto(userRepository.save(User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .surname(userDto.getSurname())
                .email(userDto.getEmail())
                .address(userDto.getAddress())
                .alerting(userDto.getAlerting())
                .energyAlertingThreshold(userDto.getEnergyAlertingThreshold())
                .build())) : null;
    }

    public void deleteUser(long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new UserNotFoundException("User not found with id: " + id);
        }
    }
}
