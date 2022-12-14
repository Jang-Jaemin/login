//  로그인 컨트롤러는 로그인 서비스를 호출해서 로그인에 성공하면 홈 화면으로 이동하고,
//  로그인에 실패하면 bindingResult.reject()를 사용해서 글로벌 오류( ObjectError)를 생성한다.
//  그리고 정보를 다시 입력하도록 로그인 폼을 뷰 템플릿으로 사용한다.

package hello.login.web.login;

import hello.login.domain.login.LoginService;
import hello.login.domain.member.Member;
import hello.login.web.SessionConst;
import hello.login.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final SessionManager sessionManager;

    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") LoginForm form) {
        return "login/loginForm";
    }

//    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());

        if (loginMember == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";
        }

        //로그인 성공 처리

        //쿠키에 시간 정보를 주지 않으면 세션 쿠기(브라우저 종료시 모두 종료)
        //아래 코드는 쿠키 생성 로직이다.
        Cookie idCookie = new Cookie("memberId", String.valueOf(loginMember.getId()));
        response.addCookie(idCookie);
        return "redirect:/";
        //로그인에 성공하면 쿠키를 생성하고 httpServletResponse에 담는다.
        //쿠키 이름인 멤버 id이고, 값은 회원의 id를 담아둔다.
        //웹 브라우저는 종료 전까지 회원의 id를 서버에 계속 보내줄것이다.
    }

//    @PostMapping("/login") 기존 주석처리
    public String loginV2(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());

        if (loginMember == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";
        }

        //로그인 성공 처리

        //세션 관리자를 통해 세션을 생성하고, 회원 데이터 보관
        sessionManager.createSession(loginMember, response);

        return "redirect:/";
        //  private final SessionManager sessionManager; 주입
        //  sessionManager.createSession(loginMember, response);
        //  로그인성공시세션을등록한다. 세션에loginMember를저장해두고, 쿠키도함께발행한다.
    }

//    @PostMapping("/login")
    public String loginV3(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }
        //  세션생성과조회
        //  세션을 생성하려면 request.getSession(true)를 사용하면 된다.
        //  public HttpSession getSession(boolean create);

        //  세션의 create 옵션.
        //  request.getSession(true)
        //  세션이 있으면 기존 세션을 반환한다.
        //  세션이 없으면 새로운 세션을 생성해서 반환한다.
        //  request.getSession(false)
        //  세션이 있으면 기존 세션을 반환한다.
        //  세션이 없으면 새로운 세션을 생성하지 않는다. null을반환한다.
        //  request.getSession(): 신규세션을 생성하는 request.getSession(true)와 동일하다.

        //  세션에 로그인 회원 정보 보관
        //  session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);
        //  세션에데이터를 보관하는 방법은 request.setAttribute(..)와 비슷하다.
        //  하나의 세션에 여러 값을 저장할 수 있다.

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());

        if (loginMember == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";
        }

        //로그인 성공 처리
        //세션이 있으면 있는 세션 반환, 없으면 신규 세션을 생성
        HttpSession session = request.getSession();
        //세션에 로그인 회원 정보 보관
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        return "redirect:/";
        //  기존homeLoginV2()의@GetMapping("/") 주석처리

        //  request.getSession(false): request.getSession()를 사용하면 기본 값이 create: true이므로,
        //  로그인하지않을사용자도의미없는세션이만들어진다.
        //  따라서 세션을 찾아서 사용하는 시점에는 create: false 옵션을 사용해서 세션을 생성하지 않아야한다.
        //  session.getAttribute(SessionConst.LOGIN_MEMBER): 로그인 시점에 세션에 보관한 회원 객체를 찾는다.

    }

    //  로그인에 성공하면 처음 요청한 URL로 이동하는 기능을 만드는 코드.
    //  로그인 체크 필터에서, 미인증 사용자는 요청 경로를 포함해서/login에redirectURL요청 파라미터를 추가해서 요청했다.
    //  이 값을 사용해서 로그인 성공시 해당 경로로 고객을 redirect한다.
    @PostMapping("/login")
    public String loginV4(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult,
                          @RequestParam(defaultValue = "/") String redirectURL,
                          HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());

        if (loginMember == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";
        }

        //로그인 성공 처리
        //세션이 있으면 있는 세션 반환, 없으면 신규 세션을 생성
        HttpSession session = request.getSession();
        //세션에 로그인 회원 정보 보관
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        return "redirect:" + redirectURL;

    }

//    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        expireCookie(response, "memberId");
        return "redirect:/";
    }

//    @PostMapping("/logout")
    public String logoutV2(HttpServletRequest request) {
        sessionManager.expire(request);
        return "redirect:/";
    }
    //  로그아웃 기능
    //  로그아웃도 응답 쿠키를 생성하는데 Max-Age=0을 확인할 수 있다.
    //  해당 쿠키는 즉시 종료된다.
    @PostMapping("/logout")
    public String logoutV3(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    private void expireCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

//  쿠키와 보안 문제
//  쿠키를 사용해서 로그인Id를 전달해서 로그인을 유지할 수 있었다.
//  그런데 여기에는 심각한 보안 문제가있다.

//  보안문제
//  쿠키 값은 임의로 변경할 수 있다.
//  클라이언트가 쿠키를 강제로 변경하면 다른 사용자가 된다.
//  실제 웹 브라우저 개발자 모드  Application Cookie 변경으로 확인 Cookie: memberId=1   Cookie: memberId=2 (다른 사용자의 이름이보임)

//  쿠키에 보관된 정보는 훔쳐갈 수 있다.
//  만약 쿠키에 개인 정보나, 신용카드 정보가 있다면?
//  이 정보가 웹 브라우저에도 보관되고, 네트워크 요청마다 계속 클라이언트에서 서버로전달된다.
//  쿠키의 정보가 나의 로컬 PC에서 털릴수도 있고, 네트워크 전송구간에서 털릴수도 있다.

//  해커가 쿠키를 한번 훔쳐가면 평생 사용할 수 있다.
//  해커가 쿠키를 훔쳐가서 그 쿠키로 악의적인 요청을 계속 시도할 수 있다.

//  대안
//  쿠키에 중요한 값을 노출하지않고, 사용자 별로 예측 불가능한 임의의 토큰(랜덤값)을노출하고, 서버에서 토큰과사용자 id를매핑해서인식한다. 그리고서버에서토큰을관리한다.
//  토큰은 해커가 임의의 값을 넣어도 찾을수 없도록 예상 불가능 해야한다.
//  해커가 토큰을 털어가도 시간이 지나면 사용할 수 없도록 서버에서 해당 토큰의 만료시간을
//  짧게(예: 30분) 유지한다.
//  또는 해킹이 의심되는 경우 서버에서 해당 토큰을 강제로 제거하면 된다.