package com.example.miaosha1.Service;

import com.example.miaosha1.dao.UserDao;
import com.example.miaosha1.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserDao userDao;

    public User getById(int id){
        return userDao.getById(id);
    }

    //@Transactional//事务支持，回滚...
    public boolean tx() {
        User user1=new User();
        user1.setId(2);
        user1.setName("222");
        userDao.insert(user1);

        User user2=new User();
        user2.setId(1);
        user2.setName("111");
        userDao.insert(user2);

        return true;
    }
}
