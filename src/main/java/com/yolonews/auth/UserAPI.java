package com.yolonews.auth;

import com.google.inject.Inject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * @author saket.mehta
 */
@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserAPI {
    private final UserService userService;
    private final AuthService authService;

    @Inject
    public UserAPI(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GET
    @Path("/{userId}")
    public Response fetchUser(@PathParam("userId") Long userId) {
        Optional<User> user = userService.fetchUser(userId).map(u -> {
            u.setPassword(null);
            return u;
        });
        if (user.isPresent()) {
            return Response.ok(user).build();
        } else {
            throw new NotFoundException();
        }
    }

    @POST
    public Response createUser(UserDTO dto) {
        if (dto.email == null || dto.username == null) {
            throw new BadRequestException("email/username is empty");
        }
        if (dto.password == null) {
            throw new BadRequestException("password is empty");
        }
        OptionalLong userId = userService.createUser(dto.toUser());
        if (userId.isPresent()) {
            return Response.ok(userId.getAsLong()).build();
        } else {
            throw new BadRequestException("could not create new user");
        }
    }

    @POST
    @Path("/{userId}")
    @Secured
    public Response updateUser(@PathParam("userId") Long userId, UserDTO dto,
                               @Context SecurityContext context) {
        Long currentUserId = Long.valueOf(context.getUserPrincipal().getName());
        if (!currentUserId.equals(userId)) {
            throw new ForbiddenException();
        }
        boolean validPassword = authService.validatePassword(dto.username, dto.password);
        if (!validPassword) {
            throw new BadRequestException("password is incorrect");
        }
        userService.updateUser(userId, dto.toUser());
        return Response.ok().build();
    }

    private static class UserDTO {
        private String username;
        private String email;
        private String password;

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        User toUser() {
            User user = new User();
            user.setPassword(password);
            user.setEmail(email);
            user.setUsername(username);
            return user;
        }
    }
}
