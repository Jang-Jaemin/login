package hello.login.web.filter;

import hello.login.web.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.PatternMatchUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class LoginCheckFilter implements Filter {

    private static final String[] whitelist = {"/", "/members/add", "/login", "/logout", "/css/*"};
    //  인증필터를적용해도홈, 회원가입, 로그인화면, css 같은리소스에는접근할수있어야한다.
    //  이렇게 화이트리스트경로는인증과무관하게항상허용한다..
    //  화이트리스트를제외한나머지모든경로에는 인증체크로직을적용한다.

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            log.info("인증 체크 필터 시작 {}", requestURI);

            if (isLoginCheckPath(requestURI)) { // isLoginCheckPath(requestURI) = 화이트리스트를제외한모든경우에인증체크로직을적용한다
                log.info("인증 체크 로직 실행 {}", requestURI);
                HttpSession session = httpRequest.getSession(false);
                if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {

                    log.info("미인증 사용자 요청 {}", requestURI);
                    //로그인으로 redirect
                    httpResponse.sendRedirect("/login?redirectURL=" + requestURI);
                    //  httpResponse.sendRedirect("/login?redirectURL=" + requestURI); = 미인증사용자는로그인화면으로리다이렉트한다.
                    //  그런데로그인이후에다시홈으로이동해버리면, 원하는경로를다시찾아가야하는불편함이있다.
                    //  예를들어서상품관리화면을보려고들어갔다가 로그인화면으로이동하면, 로그인이후에다시상품관리화면으로들어가는것이좋다.
                    //  이런부분이 개발자입장에서는좀귀찮을수있어도사용자입장으로보면편리한기능이다.
                    //  이러한기능을위해 현재요청한경로인requestURI를/login에쿼리파라미터로함께전달한다.
                    //  물론/login 컨트롤러에서로그인성공시해당경로로이동하는기능은추가로개발해야한다.

                    return; // <-- return; = 여기가중요하다. 필터를더는진행하지않는다. 이후필터는물론서블릿, 컨트롤러가더는 호출되지않는다.
                    // 앞서redirect를사용했기때문에redirect가응답으로적용되고요청이끝난다.
                }
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            throw e; //예외 로깅 가능 하지만, 톰캣까지 예외를 보내주어야 함
        } finally {
            log.info("인증 체크 필터 종료 {} ", requestURI);
        }

    }

    /**
     * 화이트 리스트의 경우 인증 체크X
     */
    private boolean isLoginCheckPath(String requestURI) {
        return !PatternMatchUtils.simpleMatch(whitelist, requestURI);
    }
}
