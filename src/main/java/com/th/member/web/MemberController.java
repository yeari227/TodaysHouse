package com.th.member.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.th.common.session.Session;
import com.th.common.util.SHA256Util;
import com.th.member.service.MemberService;
import com.th.member.validator.MemberValidator;
import com.th.member.vo.MemberVO;

@Controller
public class MemberController {

	@Autowired
	private MemberService memberService;
	
	@GetMapping("/")
	public String viewHomepage() {
		return "home";
	}
	
	@GetMapping("/member/regist")
	public String viewMemberRegistPage() {
		return "member/regist";
	}
	
	@PostMapping("/member/regist")
	@ResponseBody
	public Map<String, Object> doMemberRegistAction(@Validated({MemberValidator.Regist.class}) @ModelAttribute MemberVO memberVO, Errors errors) {
		Map<String, Object> result = new HashMap<>();
		if(errors.hasErrors()) {
			result.put("status", false);
			return result;
		}
		
		this.memberService.registMember(memberVO);
		result.put("status", true);
		
		return result;
	}

	@GetMapping("/member/login")
	public String viewMemberLoginPage() {
		return "member/login";
	}
	
	@PostMapping("/member/login")
	@ResponseBody
	public Map<String, Object> doMemberLoginAction(@Validated({MemberValidator.Login.class}) @ModelAttribute MemberVO memberVO
													, Errors errors, HttpSession session) {
		Map<String, Object> result = new HashMap<>();
		
		if(errors.hasErrors()) {
			result.put("status", false);
			return result;
		}
		
		boolean isBlockAccount = this.memberService.isBlockUser(memberVO.getEmail());
		MemberVO loginMemberVO = null;
		
		if(!isBlockAccount) {
			loginMemberVO = this.memberService.loginMember(memberVO, session);
			
			if(loginMemberVO == null) {
				this.memberService.increaseLoginFailCount(memberVO.getEmail());
				result.put("status", false);
			} else {
				this.memberService.unblockUser(memberVO.getEmail());
				result.put("status", true);
			}
		} else {
			result.put("status", false);
		}
		
		session.setAttribute(Session.MEMBER , loginMemberVO);
		
		return result;
	}
}
