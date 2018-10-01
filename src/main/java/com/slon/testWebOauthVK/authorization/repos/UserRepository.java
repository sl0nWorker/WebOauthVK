package com.slon.testWebOauthVK.authorization.repos;

import com.slon.testWebOauthVK.authorization.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Integer> {
    User findByUserip (String userip);
}
