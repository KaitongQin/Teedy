package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.UserRequestDao;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.model.jpa.UserRequest;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Date;
import java.util.List;

/**
 * User registration request REST resources.
 */
@Path("/register_request")
public class RequestResource extends BaseResource {
    
    /**
     * Guest submits a new user registration request.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestRegister(
        @FormParam("username") String username,
        @FormParam("email") String email,
        @FormParam("password") String password
    ) {

        // 校验输入
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        ValidationUtil.validateUsername(username, "username");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        email = ValidationUtil.validateLength(email, "email", 1, 100);
        ValidationUtil.validateEmail(email, "email");

        // 检查是否已存在同名用户或请求
        UserDao userDao = new UserDao();
        if (userDao.getActiveByUsername(username) != null) {
            throw new ClientException("AlreadyExistingUsername", "Login already used");
        }
        UserRequestDao userRequestDao = new UserRequestDao();
        if (userRequestDao.findPendingByUsername(username) != null) {
            throw new ClientException("AlreadyRequested", "A request with this username is already pending");
        }

        // 保存请求
        UserRequest req = new UserRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(password); // 明文存储，管理员审核通过后再hash
        req.setStatus("pending");
        req.setCreateDate(new Date());
        userRequestDao.create(req);

        JsonObjectBuilder response = Json.createObjectBuilder().add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Admin: list all user registration requests.
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUserRequests() {
        if (!authenticate()) throw new ForbiddenClientException();
        checkBaseFunction(BaseFunction.ADMIN);

        UserRequestDao userRequestDao = new UserRequestDao();
        List<UserRequest> reqs = userRequestDao.findAll();
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (UserRequest req : reqs) {
            arr.add(Json.createObjectBuilder()
                .add("id", req.getId())
                .add("username", req.getUsername())
                .add("email", req.getEmail())
                .add("status", req.getStatus())
                .add("create_date", req.getCreateDate().getTime())
            );
        }
        JsonObjectBuilder response = Json.createObjectBuilder().add("requests", arr);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Admin: accept a user registration request.
     */
    @POST
    @Path("{id}/accept")
    @Produces(MediaType.APPLICATION_JSON)
    public Response acceptUserRequest(@PathParam("id") String id) {
        if (!authenticate()) throw new ForbiddenClientException();
        checkBaseFunction(BaseFunction.ADMIN);

        UserRequestDao userRequestDao = new UserRequestDao();
        UserRequest req = userRequestDao.getById(id);
        if (req == null || !"pending".equals(req.getStatus())) {
            throw new ClientException("RequestNotFound", "Request not found or already processed");
        }

        // 创建用户
        UserDao userDao = new UserDao();
        User user = new User();
        user.setRoleId(Constants.DEFAULT_USER_ROLE);
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setEmail(req.getEmail());
        user.setStorageQuota(1000000L); // 默认1GB，可根据实际调整
        user.setOnboarding(true);
        try {
            userDao.create(user, principal.getId());
        } catch (Exception e) {
            throw new ClientException("AlreadyExistingUsername", "Login already used", e);
        }

        // 更新请求状态
        req.setStatus("accepted");
        userRequestDao.update(req);

        JsonObjectBuilder response = Json.createObjectBuilder().add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Admin: reject a user registration request.
     */
    @POST
    @Path("{id}/reject")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rejectUserRequest(@PathParam("id") String id) {
        if (!authenticate()) throw new ForbiddenClientException();
        checkBaseFunction(BaseFunction.ADMIN);

        UserRequestDao userRequestDao = new UserRequestDao();
        UserRequest req = userRequestDao.getById(id);
        if (req == null || !"pending".equals(req.getStatus())) {
            throw new ClientException("RequestNotFound", "Request not found or already processed");
        }
        req.setStatus("rejected");
        userRequestDao.update(req);

        JsonObjectBuilder response = Json.createObjectBuilder().add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
} 