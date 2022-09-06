//  세션정보와타임아웃설정

//  세션정보확인
//  세션이제공하는정보들을확인해보자.

package hello.login.web.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

@Slf4j
@RestController
public class SessionInfoController {

    @GetMapping("/session-info")
    public String sessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "세션이 없습니다.";
        }

        //세션 데이터 출력
        session.getAttributeNames().asIterator()
                .forEachRemaining(name -> log.info("session name={}, value={}", name, session.getAttribute(name)));

        log.info("sessionId={}", session.getId());
        log.info("getMaxInactiveInterval={}", session.getMaxInactiveInterval());
        log.info("creationTime={}", new Date(session.getCreationTime()));
        log.info("lastAccessedTime={}", new Date(session.getLastAccessedTime()));
        log.info("isNew={}", session.isNew());

        return "세션 출력";
        //  sessionId: 세션Id, JSESSIONID의값이다.
        //  예) 34B14F008AA3527C9F8ED620EFD7A4E1 maxInactiveInterval: 세션의유효시간, 예) 1800초, (30분)

        //  creationTime: 세션생성일시
        //  lastAccessedTime : 세션과연결된사용자가최근에서버에접근한시간, 클라이언트에서서버로 sessionId( JSESSIONID)를요청한경우에갱신된다.
        //  isNew: 새로생성된세션인지, 아니면이미과거에만들어졌고, 클라이언트에서서버로 sessionId( JSESSIONID)를요청해서조회된세션인지여부
    }
}
