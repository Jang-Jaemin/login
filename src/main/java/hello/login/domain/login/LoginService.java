//  로그인기능
//  로그인 기능을 개발. 지금은 로그인 ID, 비밀번호를 입력하는 부분에 집중.

package hello.login.domain.login;

import hello.login.domain.member.Member;
import hello.login.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;

    /**
     * @return null 로그인 실패
     */
    public Member login(String loginId, String password) {
        return memberRepository.findByLoginId(loginId)
                .filter(m -> m.getPassword().equals(password))
                .orElse(null);
    }
}

//  로그인의 핵심 비즈니스로 직은 회원을 조회한 다음에 파라미터로 넘어온 password와 비교해서 같으면 회원을반환하고,
//  만약 password가 다르면 null을 반환한다.