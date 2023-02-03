package com.snee.transactio.alexa;

import com.amazon.ask.Skills;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.servlet.SkillServlet;

public class AlexaSkillServlet extends SkillServlet {

	public AlexaSkillServlet(String skillId, RequestHandler[] handlers) {
		super(Skills.standard()
				.addRequestHandlers(handlers)
				.withSkillId(skillId)
				.build());
	}
}
