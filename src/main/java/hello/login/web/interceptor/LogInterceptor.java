//  스프링 인터셉터 - 소개 및 정의
//  스프링인터셉터도서블릿필터와같이웹과관련된공통관심사항을효과적으로해결할수있는기술이다.
//  서블릿필터가서블릿이제공하는기술이라면, 스프링인터셉터는스프링 MVC가제공하는기술이다.
//  둘다 웹과관련된공통관심사항을처리하지만, 적용되는순서와범위, 그리고사용방법이다르다.

//  스프링 인터셉터의 흐름 = HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 -> 컨트롤러
//  스프링인터셉터는 디스패처 서블릿과 컨트롤러 사이에서 컨트롤러 호출 직전에 호출된다.
//  스프링인터셉터는스프링 MVC가 제공하는 기능이기 때문에 결국 디스패처서블릿이후에 등장하게 된다.
//  스프링 MVC의 시작점이 디스패처서블릿이라고 생각해보면 이해가 될 것이다.
//  스프링인터셉터에도 URL 패턴을 적용할 수 있는데, 서블릿 URL 패턴과는다르고, 매우 정밀하게 설정할 수 있다.

//  스프링 인터셉터 제한 = HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 -> 컨트롤러 //로그인 사용자
//  HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터(적절하지 않은 요청이라 판단, 컨트롤러 호출 X) // 비 로그인 사용자
//  인터셉터에서 적절하지 않은 요청이라고 판단하면 거기에서 끝을 낼 수도 있다.그래서 로그인 여부를 체크하기 정말 좋다.

//  스프링 인터셉터 체인 = HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 인터셉터1 -> 인터셉터2 -> 컨트롤러
//  스프링인터셉터는체인으로구성되는데, 중간에인터셉터를자유롭게추가할수있다.
//  예를들어서로그를 남기는인터셉터를먼저적용하고, 그다음에로그인여부를체크하는인터셉터를만들수있다.


package hello.login.web.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    public static final String LOG_ID = "logId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        String uuid = UUID.randomUUID().toString();

        request.setAttribute(LOG_ID, uuid);

        //@RequestMapping: HandlerMethod
        //정적 리소스: ResourceHttpRequestHandler
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;//호출할 컨트롤러 메서드의 모든 정보가 포함되어 있다.
        }

        log.info("REQUEST [{}][{}][{}]", uuid, requestURI, handler);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle [{}]", modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestURI = request.getRequestURI();
        String logId = (String) request.getAttribute(LOG_ID);
        log.info("RESPONSE [{}][{}][{}]", logId, requestURI, handler);
        if (ex != null) {
            log.error("afterCompletion error!!", ex);
        }

    }
}
