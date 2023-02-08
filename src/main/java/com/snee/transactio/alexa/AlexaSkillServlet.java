package com.snee.transactio.alexa;

import com.amazon.ask.Skills;
import com.amazon.ask.servlet.SkillServlet;
import com.snee.transactio.alexa.handler.BaseRequestHandler;

import java.util.List;

/**
 * The servlet that handles the incoming Alexa Cloud requests.
 */
public class AlexaSkillServlet extends SkillServlet {

    /**
     * Initializes a standard skill with the provided request handlers.
     *
     * @param skillId  The skill id.
     * @param handlers The registered request skill handlers to initialize the skill with.
     */
    public AlexaSkillServlet(
            String skillId,
            List<BaseRequestHandler> handlers
    ) {
        super(Skills.standard()
                .addRequestHandlers(handlers.toArray(new BaseRequestHandler[0]))
                .withSkillId(skillId)
                .build());
    }
}
