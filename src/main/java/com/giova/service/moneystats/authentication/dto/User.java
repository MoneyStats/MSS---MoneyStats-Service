package com.giova.service.moneystats.authentication.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.generic.GenericDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends GenericDTO {
    @NotNull
    private String name;
    @NotNull
    private String surname;
    @NotNull
    private String email;
    @NotNull
    private String username;
    @NotNull
    private String password;
    private UserRole role;
    @NotNull
    private String profilePhoto;
    @NotNull
    private String currency;
}
