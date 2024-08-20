const container = document.getElementById('container');
const registerBtn = document.getElementById('register');
const loginBtn = document.getElementById('login');

registerBtn.addEventListener('click', () => {
    container.classList.add("active");
    console.log("test")
});

loginBtn.addEventListener('click', () => {
    container.classList.remove("active");
});

// 회원가입 관련 로직

document.getElementById("form-container-signup-box").addEventListener("submit", function (event) {
    event.preventDefault(); // 기본 동작 중지

    const password = document.getElementById("signup-password").value;
    const checkPassword = document.getElementById("signup-checkPassword").value;

    // 비밀번호와 비밀번호 확인이 일치하는지 확인
    if (password !== checkPassword) {
        alert("비밀번호가 일치하지 않습니다. 다시 확인해주세요.");
        return; // 함수 종료
    }

    const formData = {
        email: document.getElementById("signup-email").value,
        password: password,
        phone: document.getElementById("signup-phone").value,
        address: document.getElementById("signup-address").value,
        birth: document.getElementById("signup-birth").value,
        role: document.getElementById("signup-role").value,
        postcode: document.getElementById("signup-postcode").value
    };

    console.log(formData)

    fetch("/api/users/signup", { // 서버에 POST 요청 보내기
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            response.json(); // JSON 형식으로 변환된 응답을 반환
        })
        .then(data => {
            console.log("서버에서 받은 데이터:", data);
            alert("회원가입에 성공했습니다.");
            window.location.href = "/"; // 홈페이지로 이동
        })
        .catch(error => {
            alert("회원가입에 실패했습니다. 입력 정보를 확인해주세요.");
            console.error("회원가입 실패:", error);
        });
});

// 로그인 관련 로직

document.getElementById("form-container-box").addEventListener("submit", async function (event) {
    event.preventDefault(); // 기본 동작 중지
    const formData = {
        email: document.getElementById("login-email").value,
        password: document.getElementById("login-password").value,
    };

    try {
        const response = await fetch("/api/users/login", {
            method: "POST",
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(formData)
        });

        const data = await response.json();

        if (response.ok) {
            window.location.href = "/auctions"; // 홈페이지로 이동
        } else {
            document.getElementById('login-failed').innerHTML = `<font size=2>${data.data}</>`;
        }
    } catch (error) {
        console.log("네트워크 오류 발생:", error);

        alert("네트워크 오류 발생. 다시 시도해주세요.");
    }
});
