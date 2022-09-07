//  서블릿 필터 - 요청 로그
//  필터가 정말 수문장 역할을 잘 하는지 확인하기 위해 가장 단순한 필터인, 모든 요청을
//  로그로 남기는 필터를 개발한다.

package hello.login.web.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LogFilter implements Filter { // <- 필터를 사용하려면 필터 인터페이를 구현해야 한다.

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("log filter init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("log filter doFilter");

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        String uuid = UUID.randomUUID().toString(); // <-- HTTP 요청을 구분하기 위해 요청당 임의의 uuid를 생성해둔다.

        try {
            log.info("REQUEST [{}][{}]", uuid, requestURI);
            chain.doFilter(request, response); // <-- 이부분이가장중요하다. 다음필터가있으면필터를호출하고, 필터가없으면서블릿을호출한다.
                                               // 만약이로직을호출하지않으면다음단계로진행되지않는다.
        } catch (Exception e) {
            throw e;
        } finally {
            log.info("RESPONSE [{}][{}]", uuid, requestURI);
        }

    }

    @Override
    public void destroy() {
        log.info("log filter destroy");
    }
}

//  서블릿 필터
//  공통관심사항
//  요구사항을 보면 로그인한 사용자만 상품 관리 페이지에 들어갈 수 있어야 한다.
//  앞에서 로그인을 하지 않은 사용자에게는 상품 관리 버튼이 보이지 않기 때문에 문제가 없어 보인다.
//  그런데 문제는 로그인하지않은 사용자도 다음 URL을 직접 호출하면 상품 관리화면에 들어갈 수 있다는 점이다.

//  서블릿 필터 소개
//  필터의 흐름 = HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러
//  필터를적용하면필터가호출된다음에서블릿이호출된다.
//  그래서모든고객의요청로그를남기는 요구사항이있다면필터를사용하면된다.
//  참고로필터는특정 URL 패턴에적용할수있다. /* 이라고 하면모든요청에필터가적용된다.
//  참고로스프링을사용하는경우여기서말하는서블릿은스프링의디스패처서블릿으로생각하면된다.

//  필터 제한
//  HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러 //로그인 사용자
//  HTTP 요청 -> WAS -> 필터(적절하지 않은 요청이라 판단, 서블릿 호출X) //비 로그인 사용자
//  필터에서적절하지않은요청이라고판단하면거기에서끝을낼수도있다.
//  그래서로그인여부를 체크하기에딱좋다.

//  필터 체인 = HTTP 요청 -> WAS -> 필터1 -> 필터2 -> 필터3 -> 서블릿 -> 컨트롤러
//  필터는체인으로구성되는데, 중간에필터를자유롭게추가할수있다.
//  예를들어서로그를남기는필터를 먼저적용하고, 그다음에로그인여부를체크하는필터를만들수있다.
