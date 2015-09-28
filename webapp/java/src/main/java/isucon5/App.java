package isucon5;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;

import isucon5.model.*;
import isucon5.repository.*;

@SpringBootApplication
@Controller
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	ServletContextInitializer servletContextInitializer() {
		// Sessionの維持をCookieのみにして、URLにjsessionidが付かないようにする
		return servletContext -> servletContext.setSessionTrackingModes(
				Collections.singleton(SessionTrackingMode.COOKIE));
	}

	@Bean
	IDialect java8TimeDialect() {
		// ThymeleafでJava8のDate and Time APIをフォーマットするための設定
		return new Java8TimeDialect();
	}

	@Autowired
	UserRepository userRepository;
	@Autowired
	ProfileRepository profileRepository;
	@Autowired
	RelationRepository relationRepository;
	@Autowired
	EntryRepository entryRepository;
	@Autowired
	CommentRepository commentRepository;
	@Autowired
	FootprintRepository footprintRepository;
	@Autowired
	Isucon5qInitializer initializer;

	@Autowired // 本当は使いたくないけど、他言語と実装方法を揃えるために使用する
	HttpSession session;
	@Autowired
	HttpServletRequest request;

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	String login(Model model) {
		model.addAttribute("message", "高負荷に耐えられるSNSコミュニティサイトへようこそ!");
		return "login";
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	String login(@RequestParam("email") String email,
			@RequestParam("password") String password) {
		authenticate(email, password);
		request.changeSessionId(); // Session Fixation対策
		return "redirect:/";
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	String logout() {
		try {
			session.invalidate();
		}
		catch (IllegalStateException ignored) {
		}
		return "redirect:/login";
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	String home(Model model) {
		if (!isAuthenticated()) {
			return "redirect:/login";
		}

		Integer userId = currentUser().getId();
		Profile profile = profileRepository.findByUserId(userId);
		List<Entry> entries = entryRepository.findByUserIdOrderByCreatedAtDesc(userId,
				10);
		List<Comment> commentsForMe = commentRepository.findByUserId(userId);
		// 友達のエントリをアプリ側でフィルタして10件取得
		List<Entry> entriesOfFriends = entryRepository.findOrderByCreatedAtDesc(
				stream -> stream.filter(entry -> isFriend(entry.getUserId())).limit(10)
						.collect(Collectors.toList()));
		// 友達のコメントをアプリ側でフィルタして10件取得
		List<Comment> commentsOfFriends = commentRepository
				.findOrderByCreatedAtDesc(stream -> stream.filter(comment -> {
					if (!isFriend(comment.getUserId())) {
						return false;
					}
					Entry entry = entryRepository.findOne(comment.getEntryId());
					return !(entry.isPrivate() && !isPermitted(entry.getUserId()));
				}).limit(10).collect(Collectors.toList()));
		// Relationのリストから、keyがログユーザーじゃない方のuserId, valueが作成時刻なMapを作成
		Map<Integer, LocalDateTime> friends = relationRepository
				.findByUserIdOrderByCreatedAtDesc(currentUser().getId()).stream()
				.collect(Collectors.toMap(
						x -> Objects.equals(x.getOne(), currentUser().getId())
								? x.getAnother() : x.getOne(),
						Relation::getCreatedAt, (u, v) -> u, LinkedHashMap::new));
		List<Footprint> footprints = footprintRepository.findByUserId(userId);

		model.addAttribute("profile", profile);
		model.addAttribute("entries", entries);
		model.addAttribute("friends", friends);
		model.addAttribute("comments_for_me", commentsForMe);
		model.addAttribute("entries_of_friends", entriesOfFriends);
		model.addAttribute("comments_of_friends", commentsOfFriends);
		model.addAttribute("footprints", footprints);
		return "index";
	}

	@RequestMapping(value = "/profile/{accountName}", method = RequestMethod.GET)
	String profile(@PathVariable("accountName") String accountName, Model model) {
		if (!isAuthenticated()) {
			return "redirect:/login";
		}

		User owner = userFromAccount(accountName);
		Integer ownerId = owner.getId();
		Profile profile = profileRepository.findByUserId(ownerId);
		if (profile == null) {
			profile = new Profile();
		}
		List<Entry> entries = isPermitted(ownerId)
				? entryRepository.findByUserIdOrderByCreatedAtDesc(ownerId, 5)
				: entryRepository.findByUserIdAndNotPrivateOrderByCreatedAtDesc(ownerId,
						5);
		markFootprint(ownerId);

		model.addAttribute("owner", owner);
		model.addAttribute("currentUser", currentUser());
		model.addAttribute("profile", profile);
		model.addAttribute("entries", entries);
		model.addAttribute("private", isPermitted(ownerId));
		model.addAttribute("friend", isFriend(ownerId));
		model.addAttribute("prefectures",
				("北海道 青森県 岩手県 宮城県 秋田県 山形県 福島県 茨城県 栃木県 群馬県 埼玉県 千葉県 東京都 神奈川県 新潟県 富山県 "
						+ "石川県 福井県 山梨県 長野県 岐阜県 静岡県 愛知県 三重県 滋賀県 京都府 大阪府 兵庫県 奈良県 和歌山県 鳥取県 島根県 "
						+ "岡山県 広島県 山口県 徳島県 香川県 愛媛県 高知県 福岡県 佐賀県 長崎県 熊本県 大分県 宮崎県 鹿児島県 沖縄県")
								.split(" "));
		return "profile";
	}

	@RequestMapping(value = "/profile/{accountName}", method = RequestMethod.POST)
	String profile(@PathVariable("accountName") String accountName,
			@RequestParam("first_name") String firstName,
			@RequestParam("last_name") String lastName, @RequestParam("sex") String sex,
			@RequestParam("pref") String pref,
			@RequestParam("birthday") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDay) {
		if (!isAuthenticated()) {
			return "redirect:/login";
		}
		if (!Objects.equals(accountName, currentUser().getAccountName())) {
			throw new PermissionDenied();
		}

		Profile profile = profileRepository.findByUserId(currentUser().getId());
		if (profile == null) {
			profile = new Profile();
		}
		profile.setFirstName(firstName);
		profile.setLastName(lastName);
		profile.setSex(sex);
		profile.setPref(pref);
		profile.setBirthDay(birthDay);
		if (profile.getUserId() != null) {
			profileRepository.update(profile);
		}
		else {
			profile.setUserId(currentUser().getId());
			profileRepository.create(profile);
		}
		return "redirect:/profile/{accountName}";
	}

	@RequestMapping(value = "/diary/entries/{accountName}", method = RequestMethod.GET)
	String entries(@PathVariable("accountName") String accountName, Model model) {
		if (!isAuthenticated()) {
			return "redirect:/login";
		}

		User owner = userFromAccount(accountName);
		Integer ownerId = owner.getId();
		List<Entry> entries = isPermitted(ownerId)
				? entryRepository.findByUserIdOrderByCreatedAtDesc(ownerId, 20)
				: entryRepository.findByUserIdAndNotPrivateOrderByCreatedAtDesc(ownerId,
						20);
		markFootprint(ownerId);

		model.addAttribute("owner", owner);
		model.addAttribute("entries", entries);
		model.addAttribute("myself", Objects.equals(currentUser().getId(), ownerId));
		return "entries";
	}

	@RequestMapping(value = "/diary/entry/{entryId}", method = RequestMethod.GET)
	String entry(@PathVariable("entryId") Integer entryId, Model model) {
		if (!isAuthenticated()) {
			return "redirect:/login";
		}

		Entry entry = entryRepository.findOne(entryId);
		if (entry == null) {
			throw new ContentNotFound();
		}
		User owner = getUser(entry.getUserId());
		if (entry.isPrivate() && !isPermitted(owner.getId())) {
			throw new PermissionDenied();
		}
		List<Comment> comments = commentRepository.findByEntryId(entryId);
		markFootprint(owner.getId());

		model.addAttribute("owner", owner);
		model.addAttribute("entry", entry);
		model.addAttribute("comments", comments);
		return "entry";
	}

	@RequestMapping(value = "/diary/entry", method = RequestMethod.POST)
	String entry(@RequestParam(value = "title", defaultValue = "タイトルなし") String title,
			@RequestParam("content") String content,
			@RequestParam(name = "private", defaultValue = "false") boolean isPrivate,
			RedirectAttributes attributes) {
		if (!isAuthenticated()) {
			return "redirect:/login";
		}

		Entry entry = new Entry();
		entry.setUserId(currentUser().getId());
		entry.setBody(title + "\n" + content);
		entry.setPrivate(isPrivate);
		entryRepository.create(entry);

		attributes.addAttribute("accountName", currentUser().getAccountName());
		return "redirect:/diary/entries/{accountName}";
	}

	@RequestMapping(value = "/diary/comment/{entryId}", method = RequestMethod.POST)
	String comment(@PathVariable("entryId") Integer entryId,
			@RequestParam("comment") String comment) {
		if (!isAuthenticated()) {
			return "redirect:/login";
		}

		Entry entry = entryRepository.findOne(entryId);
		if (entry == null) {
			throw new ContentNotFound();
		}
		if (entry.isPrivate() && !isPermitted(entry.getUserId())) {
			throw new PermissionDenied();
		}

		Comment com = new Comment();
		com.setEntryId(entryId);
		com.setUserId(currentUser().getId());
		com.setComment(comment);
		commentRepository.create(com);

		return "redirect:/diary/entry/{entryId}";
	}

	@RequestMapping(value = "/footprints", method = RequestMethod.GET)
	String footprints(Model model) {
		if (!isAuthenticated()) {
			return "redirect:/login";
		}

		List<Footprint> footprints = footprintRepository
				.findByUserId(currentUser().getId());

		model.addAttribute("footprints", footprints);
		return "footprints";
	}

	@RequestMapping(value = "/friends", method = RequestMethod.GET)
	String friends(Model model) {
		if (!isAuthenticated()) {
			return "redirect:/login";
		}

		// Relationのリストから、keyがログユーザーじゃない方のuserId, valueが作成時刻なMapを作成
		Map<Integer, LocalDateTime> friends = relationRepository
				.findByUserIdOrderByCreatedAtDesc(currentUser().getId()).stream()
				.collect(Collectors.toMap(
						x -> Objects.equals(x.getOne(), currentUser().getId())
								? x.getAnother() : x.getOne(),
						Relation::getCreatedAt, (u, v) -> u, LinkedHashMap::new));

		model.addAttribute("friends", friends);
		return "friends";
	}

	@RequestMapping(value = "/friends/{accountName}", method = RequestMethod.POST)
	String friends(@PathVariable("accountName") String accountName) {
		if (!isAuthenticated()) {
			return "redirect:/login";
		}

		if (!isFriendAccount(accountName)) {
			User user = userFromAccount(accountName);
			if (user == null) {
				throw new ContentNotFound();
			}
			Relation relation = new Relation();
			relation.setOne(currentUser().getId());
			relation.setAnother(user.getId());
			relationRepository.create(relation);
		}

		return "redirect:/friends";
	}

	@RequestMapping(value = "/initialize", method = RequestMethod.GET)
	public ResponseEntity<String> initialize() {
		initializer.initialize();
		return ResponseEntity.ok("");
	}

	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(AuthenticationError.class)
	ModelAndView authenticationError() {
		return new ModelAndView("login").addObject("message", "ログインに失敗しました");
	}

	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(PermissionDenied.class)
	ModelAndView permissionDenied() {
		return new ModelAndView("error").addObject("message", "友人のみしかアクセスできません");
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(ContentNotFound.class)
	ModelAndView contentNotFound() {
		return new ModelAndView("error").addObject("message", "要求されたコンテンツは存在しません");
	}

	// 以下、他言語実装と合わせるためのメソッド群

	void authenticate(String email, String password) {
		User user = userRepository.findByEmailAndRawPassword(email, password);
		if (user == null) {
			throw new AuthenticationError();
		}
		session.setAttribute("user_id", user.getId());
	}

	User currentUser() {
		Object user = request.getAttribute("user");
		if (user != null) {
			return User.class.cast(user);
		}
		Object userId = session.getAttribute("user_id");
		if (userId == null) {
			return null;
		}
		User current = getUser(Integer.class.cast(userId));
		if (current == null) {
			throw new AuthenticationError();
		}
		request.setAttribute("user", current);
		return current;
	}

	boolean isAuthenticated() {
		return currentUser() != null;
	}

	User getUser(Integer userId) {
		User user = userRepository.findOne(userId);
		if (user == null) {
			throw new ContentNotFound();
		}
		return user;
	}

	User userFromAccount(String accountName) {
		User user = userRepository.findByAccountName(accountName);
		if (user == null) {
			throw new ContentNotFound();
		}
		return user;
	}

	boolean isFriend(Integer anotherId) {
		Integer userId = Integer.class.cast(session.getAttribute("user_id"));
		return relationRepository.countByOneAndAnother(userId, anotherId) > 0;
	}

	boolean isFriendAccount(String accountName) {
		return isFriend(userFromAccount(accountName).getId());
	}

	boolean isPermitted(Integer anotherId) {
		return Objects.equals(anotherId, currentUser().getId()) || isFriend(anotherId);
	}

	void markFootprint(Integer userId) {
		if (!Objects.equals(userId, currentUser().getId())) {
			Footprint footprint = new Footprint();
			footprint.setUserId(userId);
			footprint.setOwnerId(currentUser().getId());
			footprintRepository.create(footprint);
		}
	}

	public static class AuthenticationError extends RuntimeException {
	}

	public static class PermissionDenied extends RuntimeException {
	}

	public static class ContentNotFound extends RuntimeException {
	}

	@Repository
	public static class Isucon5qInitializer {
		@Autowired
		JdbcTemplate jdbcTemplate;

		@Transactional
		public void initialize() {
			jdbcTemplate.update("DELETE FROM relations WHERE id > 500000");
			jdbcTemplate.update("DELETE FROM footprints WHERE id > 500000");
			jdbcTemplate.update("DELETE FROM entries WHERE id > 500000");
			jdbcTemplate.update("DELETE FROM comments WHERE id > 1500000");
		}
	}
}
