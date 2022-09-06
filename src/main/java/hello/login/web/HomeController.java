//  홈 컨트롤러 - 로직분석
//  로그인 쿠키(memberId)가 없는 사용자는 기존 home으로 보낸다.
//  추가로 로그인 쿠키가 있어도 회원이 업승면 home으로 보낸다.
//  로그인쿠키(memberId)가 있는 사용자는 로그인 사용자 전용 홈 화면인 loginHome으로 보낸다.
//  추가로 홈 화면에 화원 관련 정보도 출력 해야해서 member 데이터도 모델에 담아서 전달한다.

package hello.login.web;

import hello.login.domain.member.Member;
import hello.login.domain.member.MemberRepository;
import hello.login.web.argumentresolver.Login;
import hello.login.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberRepository memberRepository;
    private final SessionManager sessionManager;

//    @GetMapping("/")
    public String home() {
        return "home";
    }

//    @GetMapping("/")
    public String homeLogin(@CookieValue(name = "memberId", required = false) Long memberId, Model model) {

        if (memberId == null) {
            return "home";
        }

        //로그인
        Member loginMember = memberRepository.findById(memberId);
        if (loginMember == null) {
            return "home";
        }

        model.addAttribute("member", loginMember);
        return "loginHome";
    }

//    @GetMapping("/")
    public String homeLoginV2(HttpServletRequest request, Model model) {

        //세션 관리자에 저장된 회원 정보 조회
        Member member = (Member)sessionManager.getSession(request);

        //로그인
        if (member == null) {
            return "home";
        }

        model.addAttribute("member", member);
        return "loginHome";
        //  private final SessionManager sessionManager; 주입
        //  기존homeLogin()의@GetMapping("/") 주석처리
        //  세션관리자에서저장된회원정보를조회한다.
        //  만약회원정보가없으면, 쿠키나세션이없는것이므로 로그인되지않은것으로처리한다.
    }

//    @GetMapping("/")
    public String homeLoginV3(HttpServletRequest request, Model model) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return "home";
        }

        Member loginMember = (Member)session.getAttribute(SessionConst.LOGIN_MEMBER);

        //세션에 회원 데이터가 없으면 home
        if (loginMember == null) {
            return "home";
        }

        //세션이 유지되면 로그인으로 이동
        model.addAttribute("member", loginMember);
        return "loginHome";
    }

    //  @SessionAttribute
    //  스프링은 세션을 더 편리하게 사용할 수 있도록 @SessionAttribute을 지원한다.
    //  이미 로그인된 사용자를 찾을때는 다음과 같이 사용하면 된다.
    //  참고로 이 기능은 세션을 생성하지 않는다.
    //  @SessionAttribute(name = "loginMember", required = false) Member loginMember

    //  homeLoginV3()의 @GetMapping("/") 주석처리
    //  세션을찾고, 세션에 들어있는 데이터를 찾는 번거로운 과정을 스프링이 한번에 편리하게 처리 해주는 것을 확인할 수 있다.

//    @GetMapping("/")
    public String homeLoginV3Spring(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember, Model model) {

        //세션에 회원 데이터가 없으면 home
        if (loginMember == null) {
            return "home";
        }

        //세션이 유지되면 로그인으로 이동
        model.addAttribute("member", loginMember);
        return "loginHome";
    }

    @GetMapping("/")
    public String homeLoginV3ArgumentResolver(@Login Member loginMember, Model model) {

        //세션에 회원 데이터가 없으면 home
        if (loginMember == null) {
            return "home";
        }

        //세션이 유지되면 로그인으로 이동
        model.addAttribute("member", loginMember);
        return "loginHome";
    }
}