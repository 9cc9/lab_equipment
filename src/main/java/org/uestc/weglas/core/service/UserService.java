package org.uestc.weglas.core.service;

import org.uestc.weglas.core.model.User;

public interface UserService {
    User login(String account, String password);
}
