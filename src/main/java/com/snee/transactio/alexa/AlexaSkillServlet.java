package com.snee.transactio.alexa;

import com.amazon.ask.Skills;
import com.amazon.ask.servlet.SkillServlet;
import com.snee.transactio.alexa.handler.BaseRequestHandler;

import java.util.List;

public class AlexaSkillServlet extends SkillServlet {

	public AlexaSkillServlet(String skillId, List<BaseRequestHandler> handlers) {
		super(Skills.standard()
				.addRequestHandlers(handlers.toArray(new BaseRequestHandler[0]))
				.withSkillId(skillId)
				.build());
	}
}
