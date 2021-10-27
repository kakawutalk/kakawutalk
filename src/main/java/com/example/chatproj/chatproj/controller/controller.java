package com.example.chatproj.chatproj.controller;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.chatproj.chatproj.domain.Chatroom_Table;
import com.example.chatproj.chatproj.domain.UC_Table;
import com.example.chatproj.chatproj.domain.User;
import com.example.chatproj.chatproj.service.UserService;
import com.example.chatproj.chatproj.service.ChatService;

@Controller
public class controller {

	private final UserService userService;
	private final ChatService chatService;
	
	@Autowired
	public controller(UserService userService, ChatService chatService) {
		this.userService = userService;
		this.chatService = chatService;
	}
	
	// 메인페이지
	@RequestMapping("/")
	public String main(HttpServletRequest request) {
		// session
		HttpSession session = request.getSession();
		String sessionName = (String)session.getAttribute("sessionId");	
		
		System.out.println("sessionName : " + sessionName);
		
		if(sessionName != null) {
			return "redirect:/chatList";
		}else {
			return "redirect:/signin";
		}
	}
	
	// 회원가입
	@RequestMapping("/signup")
	public String signup() {
		return "signup";
	}
	
	@PostMapping("/signup")
	public String create_user(SignupForm form) {
		User user = new User();
		user.setUid(form.getUid());
		user.setUpw(form.getUpw());
		user.setUname(form.getUname());
		user.setEmail(form.getEmail());
		user.setPhone_num(form.getPhone_num());
		
		userService.join(user);
		
		return "redirect:/signin";
	}
	
	// 로그인
	@RequestMapping("/signin")
	public String signin() {
		return "signin";
	}
	
	@PostMapping("/signin")
	public String login_user(SigninForm form, HttpServletRequest request) {
		User user = new User();
		user.setUid(form.getUid());
		user.setUpw(form.getUpw());
		
		String result = userService.login(user);
		
		System.out.println("result : " + result );
		
		if(result.equals("matchX")) {
			return "redirect:/signin?message=FAILURE_matchX";
		}else if(result.equals("noid")) {
			return "redirect:/signin?message=FAILURE_noid";
		}else {
			HttpSession session = request.getSession();
			String name = form.getUid();
			session.setAttribute("sessionId", name);
			return "redirect:/chatList";
		}	
	}
	
	// 유저 처리 페이지
	@RequestMapping("/userprocess")
	public String uesrprocess(@RequestParam("redirectprocess") String redirectprocess) {
		
		return "UserProcess";
	}

	// 아이디 찾기
	@RequestMapping("/findid")
	public String findid() {
		return "find_id";
	}
	
	@PostMapping("/findid")
	public String find_userid(FindIdForm form, RedirectAttributes redirectAttributes) {
		User user = new User();
		user.setUname(form.getUname());
		user.setEmail(form.getEmail());
		
		String redirectprocess = null;
		
		try {
			String result = userService.findUser(user);
			redirectprocess = result;
		
		}catch(NoSuchElementException e) {
			redirectprocess = "존재하지 않는 ID 입니다.";
		}
		
		System.out.println(redirectprocess);
		redirectAttributes.addAttribute("redirectprocess", redirectprocess);
		return "redirect:/userprocess";	
	}
	
	// 비밀번호 찾기
	@RequestMapping("/findpw")
	public String findpass() {
		return "find_password";
	}
	
	// 채팅방 리스트
	@RequestMapping("/chatList")
	public String chatlist(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		String sessionName = (String)session.getAttribute("sessionId");
	
		Optional<User> getSessionName = userService.getSessionbyUid(sessionName);		
		int sessionNum = getSessionName.get().getUnum();
		
		List<UC_Table> getChatList = chatService.getChatList(sessionNum);
		
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		map.put(getChatList.get(0).getCnum(), getChatList.get(0).getCname());
		
		for(int i = 0; i<getChatList.size(); i++) {
			map.put(getChatList.get(i).getCnum(), getChatList.get(i).getCname());
			System.out.println("getChatList : " + map.get(i+1));
		}
		model.addAttribute("chatlist", map);
	
		
		return "chatList";
	}
	
	
	
	
	
	// 초대
	@RequestMapping("/inviteuser")
	public String inviteuser(HttpServletRequest request) {
		// session
		HttpSession session = request.getSession();
		String sessionName = (String)session.getAttribute("sessionId");	
		
		if(sessionName != null) {
			return "InviteUser";
		}else {
			return "redirect:signin";
		}
		
	}
	
	@PostMapping("/inviteuser")
	public String invite_user(SigninForm signinform, InviteUserForm inviteuserform, HttpServletRequest request) {
		User user = new User();
		user.setUid(signinform.getUid());
		
		String id_check_result = userService.duplicateMatch(user);
		
		if(id_check_result.equals("matchX")) {
			return "redirect:/signin?message=FAILURE_matchX";
		}else if(id_check_result.equals("noid")) {
			return "redirect:/signin?message=FAILURE_noid";
		}else {
			// session
			HttpSession session = request.getSession();
			String sessionName = (String)session.getAttribute("sessionId");	
			// insert chatroom_table
			if(inviteuserform.getUid() != sessionName && !inviteuserform.getUid().equals(sessionName)) {
				Optional<Chatroom_Table> selectcnum = chatService.join();
				
				Chatroom_Table chatroom_table = new Chatroom_Table();
				
				try {
					chatroom_table.setCnum(selectcnum.get().getCnum() + 1);
					chatroom_table.setCname(inviteuserform.getCname());			
					chatService.insChatTable(chatroom_table);
				}catch(NoSuchElementException e) {
					chatroom_table.setCnum(1);
					chatroom_table.setCname(inviteuserform.getCname());			
					chatService.insChatTable(chatroom_table);		
				}
				
				// select m from user_table m where uid in ( 내 id값, 초대한 사람의 id값);
				String inviteuser = inviteuserform.getUid();
				
				List<User> GetMyIdInviteId = userService.getIdbyUid(sessionName, inviteuser);
				
				// session, invite 기반 insert			
				for(int i = 0; i<GetMyIdInviteId.size(); i++) {
					UC_Table uc_table = new UC_Table();
					uc_table.setUnum(GetMyIdInviteId.get(i).getUnum());
					try {
						uc_table.setCnum(selectcnum.get().getCnum() + 1);
						uc_table.setCname(inviteuserform.getCname());	
					}catch(NoSuchElementException e){
						Optional<Chatroom_Table> null_chatroom_table = chatService.join();		
						uc_table.setCnum(null_chatroom_table.get().getCnum());
						uc_table.setCname(inviteuserform.getCname());
					}
					chatService.insUCTable(uc_table);
				}
			
			}else {
				return "redirect:/signin?message=FAILURE_noid";				
			}
			
			return "redirect:/chatpg";
		}	
	
	}
	
	// 채팅방
	@RequestMapping("/chatpg")
	public String chatpg() {
		return "chatpg";
	}
	
}

