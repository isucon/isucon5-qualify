module Isucon5Portal
  module Score
    def self.calculate(result)
      # result = {
      #   "valid" : false,
      #   "requests" : 5,
      #   "elapsed" : 413,
      #   "done" : "BootstrapChecker",
      #   "responses" : {
      #     "success" : 2,
      #     "redirect" : 1,
      #     "failure" : 0,
      #     "error" : 0,
      #     "exception" : 2
      #   },
      #   "violations" : [
      #     {
      #       "type" : "HttpResponseException",
      #       "description" : "HTTP protocol violation: Authentication challenge without WWW-Authenticate header",
      #       "number" : 2
      #     },
      #     {
      #       "type" : "SHOULD LOGIN AT FIRST",
      #       "description" : "未ログインでトップページへのアクセスが正しいリダイレクトになっていません",
      #       "number" : 1
      #     }
      #   ]
      # }
      base_score = result["responses"]["success"] + result["responses"]["redirect"] * 0.1
      minus_score = result["responses"]["error"] * 10 + result["responses"]["exception"] * 20

      too_slow_penalty = 0
      too_slow_responses = result["violations"].select{|v| v["description"] =~ /アプリケーションが \d+ ミリ秒以内に応答しませんでした/ }
      if too_slow_responses.size > 0
        too_slow_penalty = too_slow_responses.map{|v| v["number"]}.inject(&:+) * 100
      end

      score = base_score - minus_score - too_slow_penalty
      if score < 0
        score = 0
      end

      summary = (result["valid"] ? "success" : "fail")
      if score < 1
        summary = "fail"
      end
      return summary, score
    end
  end
end
