/**
 * Copyright (c) 2022 Enaium
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cn.enaium.controller;

import cn.enaium.dto.Result;
import cn.enaium.dto.UserDTO;
import cn.enaium.entity.UserEntity;
import cn.enaium.mapper.UserMapper;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Enaium
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper mapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @RequestMapping("/login")
    @ResponseBody
    public Result<String> login(@RequestBody UserDTO userDTO) {
        var byUsernameAndPassword = mapper.getByUsernameAndPassword(userDTO.getUsername(), userDTO.getPassword());
        if (byUsernameAndPassword != null) {

            var uuid = "user-token:" + UUID.randomUUID();
            redisTemplate.opsForValue().set(uuid, byUsernameAndPassword.getId().toString(), 30, TimeUnit.MINUTES);
            return new Result<>(true, uuid);
        }
        return new Result<>(false, "wrong username or password");
    }

    @RequestMapping("/register")
    @ResponseBody
    public Result<String> register(@RequestBody UserDTO userDTO) {

        if (StringUtil.isNullOrEmpty(userDTO.getUsername()) || StringUtil.isNullOrEmpty(userDTO.getPassword())) {
            new Result<>(false, "username or password is empty");
        }

        if (mapper.getByUsername(userDTO.getUsername()) != null) {
            return new Result<>(false, "username already exists");
        } else {
            mapper.insert(new UserEntity(null, userDTO.getUsername(), userDTO.getPassword()));
            return new Result<>(true, "register success");
        }
    }

    @RequestMapping("/test")
    @ResponseBody
    private Result<String> test() {
        return new Result<>(true, "test");
    }
}
