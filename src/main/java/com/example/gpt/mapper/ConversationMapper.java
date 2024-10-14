package com.example.gpt.mapper;

import com.example.gpt.DAO.Conversation;
import com.example.gpt.DAO.ConversationOld;
import com.example.gpt.DAO.IdAndUsernameFromConversation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface ConversationMapper {
    @Insert("insert into conversation(session_id,is_deleted,create_at,user_id,username,title)" +
            "values(#{session_id},#{is_deleted},#{create_at},#{user_id},#{username},#{title})")
    public int save(Conversation conversation);
    @Select("select session_id,create_at,title from conversation where username=#{username} and is_deleted=0 order by create_at DESC")
    public List<ConversationOld> getSessionList(String username);
    @Select("select * from conversation where session_id=#{sessionId}")
    public Conversation getConversationBySessionId(String sessionId);
    @Select("select id from conversation where session_id=#{sessionId}")
    Integer getIdBySessionId(String sessionId);

    @Select("select username from conversation where session_id=#{sessionId}")
    String getUsernameBySessionId(String sessionId);

    @Select("select id,username from conversation where session_id=#{sessionId}")
    IdAndUsernameFromConversation getIdAndUsernameBySessionId(String sessionId);
    @Update("update conversation set is_deleted=#{is_deleted} where session_id=#{session_id}")
    public int deleteSession(Conversation conversation);

    @Update("update conversation set title=#{title} where session_id=#{session_id}")
    public int updateTitle(String title,String session_id);
}
