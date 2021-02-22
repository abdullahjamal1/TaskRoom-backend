package app.services;

import java.util.List;

import com.sun.mail.iap.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.models.entity.Message;
import app.models.entity.Msg_vote;
import app.models.entity.User;
import app.repositories.MessageRepository;
import app.repositories.Msg_voteRepository;
import app.models.projections.MessageInfo;

@Service
public class MessageService {

	@Autowired
	private MessageRepository messageRepo;

	@Autowired
	private Msg_voteRepository voteRepo;

	public void deleteMessageById(Long msg_id, Long user_id) {

			messageRepo.deleteById(msg_id);
	}

	public void voteMessage(Long msg_id, boolean vote, Long user_id) {

		Msg_vote msgVote = new Msg_vote();
		msgVote.setDid_upvote(vote);
		msgVote.setMsg_id(msg_id);
		msgVote.setUser_id(user_id);

		voteRepo.save(msgVote);
	}

	public boolean isMessageAuthorOrAdmin(Long msg_id, Long user_id) {

		/*
		 * if @AuthorOfGame OR @Admin allow to delete the game else deny
		 */
		if (messageRepo.findAuthorByMessageId(msg_id)== user_id /*|| loggedInUser.isAdmin()*/)
			return true;
		else
			return false;
	}

	public Message save(Message msg, Long game_id, Long parent_id, Long user_id) {

		msg.setMsg_id(null);

		msg.setUser_id(user_id);
		msg.setGame_id(game_id);
		msg.setParent_id(parent_id);

		return messageRepo.save(msg);
	}

	public List<MessageInfo> findAllMessages(Long game_id, Long parent_id) {

		return messageRepo.findAllMessagesByGameId(game_id, parent_id);
	}

}
