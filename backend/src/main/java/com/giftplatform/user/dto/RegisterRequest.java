
package com.giftplatform.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 120) String fullName,

        @NotBlank
        @Pattern(
                regexp = "^[a-z][a-z0-9_]{3,29}$",
                message = "Invalid username"
        )
        String username,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank @Pattern(regexp = "^\\+[1-9][0-9]{7,14}$", message = "Mobile must use international format") String mobile,

        @NotBlank
        @Size(min = 12)
        String password,
        String emailVerificationToken

 ) {
    public RegisterRequest(String fullName,String username,String email,String mobile,String password){this(fullName,username,email,mobile,password,null);}
    public RegisterRequest(String fullName,String username,String email,String password){this(fullName,username,email,null,password);}
    // Preserve internal constructor calls; public HTTP signup validates the email format; verification proof is optional.
    public RegisterRequest(String username,String email,String password){this(null,username,email,null,password);}
}
