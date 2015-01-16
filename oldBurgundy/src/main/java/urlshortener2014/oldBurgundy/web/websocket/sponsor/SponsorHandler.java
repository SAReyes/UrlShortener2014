package urlshortener2014.oldBurgundy.web.websocket.sponsor;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import urlshortener2014.oldBurgundy.repository.sponsor.WorksRepositorySponsor;

/**
 * Handler of sponsor web socket
 */
public class SponsorHandler extends TextWebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(SponsorHandler.class);

	@Autowired
	WorksRepositorySponsor worksRepository;

    @Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception{
		logger.info("Connected to sponsor web socket with id: " + session.getId());
	}

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
		
		String[] msg = message.getPayload().split(" ");
		
		switch(msg.length){
			case 1:
				logger.info("Solicitated shorturl: '" + msg[0].trim() + "' with id: " + session.getId());
				this.worksRepository.addWaitingClient(msg[0].trim(), session);
				break;
			default:
				logger.info("Solicitated: '" + message.getPayload() + "' with id: " + session.getId());
				try {
					session.sendMessage(new TextMessage("error::" + HttpStatus.BAD_REQUEST.getReasonPhrase()));
				} catch (IOException e) {
				}
		}		
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
		logger.info("Disconnected with id " + session.getId());
    }
}