package dev.sajiwo.panggaguard.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

import dev.sajiwo.panggaguard.dto.request.SignUpRequest;
import dev.sajiwo.panggaguard.dto.response.UserResponse;
import dev.sajiwo.panggaguard.entity.User;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE, nullValueMapMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT, nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface UserMapper {

  User map(SignUpRequest source);

  UserResponse map(User source);

}
