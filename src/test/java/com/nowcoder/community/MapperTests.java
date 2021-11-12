package com.nowcoder.community;

import com.nowcoder.community.dao.*;
import com.nowcoder.community.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setEmail("test@qq.com");
        user.setSalt("abc");
        user.setHeaderUrl("http://www.nowcode.com//101.png");
        user.setCreateTime(new Date());

        int row = userMapper.insertUser(user);
        System.out.println(row);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser(){
        int row = userMapper.updateStatus(150,1);
        System.out.println(row);

        row = userMapper.updateHeader(150,"http://www.nowcode.com//102.png");
        System.out.println(row);

        row = userMapper.updatePassword(150,"12345");
        System.out.println(row);
    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0,0,10);
        for(DiscussPost post : list){
            System.out.println(post);
        }

        int row = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(row);
    }

    @Test
    public void testInsertTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000*60*10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc",1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testInsertDiscussPost(){
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(111);
        discussPost.setTitle("test");
        discussPost.setContent("test");
        discussPost.setType(0);
        discussPost.setStatus(0);
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(0);
        discussPost.setScore(0);

        int res = discussPostMapper.insertDiscussPost(discussPost);
        System.out.println(res);
    }

    @Test
    public void testSelectComment(){
        List<Comment> list = commentMapper.selectCommentsByEntity(1,280,0,10);
        for(Comment comment : list){
            System.out.println(comment);
        }

        int rows = commentMapper.selectCountByEntity(1,280);
        System.out.println(rows);
    }

    @Test
    public void testInsertComment(){
        Comment comment = new Comment();
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        comment.setUserId(105);
        comment.setEntityId(280);
        comment.setTargetId(0);
        comment.setContent("你好你好");
        comment.setEntityType(1);
        int res = commentMapper.insertComment(comment);
        System.out.println(res);
    }

    @Test
    public void testSelectLetters(){
        List<Message> list = messageMapper.selectConversations(111,0,20);
        for(Message m : list){
            System.out.println(m);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112",0,10);
        for(Message m : list){
            System.out.println(m);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(count);
    }

}
