# Reflection 도메인 모델 (Primary vs Commentary)

**상태: 확립(Established).** 이 문서는 Reflection 의 도메인 정체성(존재론·Aggregate)을 정의하는 **영구 계약**이다. 저장 방식(테이블·마이그레이션·인프라)은 이 문서의 범위가 아니다 — [reflection-persistence-proposal.md](reflection-persistence-proposal.md) 가 다룬다.

> 도메인과 영속을 분리한다: 영속 결정이 바뀌어도(예: 저장 후보 회귀) 이 문서는 거의 불변으로 남는다.

---

## 0. 배경 — Commentary 재정의

Reflection 의 정체는 "Entry 의 한 종류"가 아니라 **다른 층**이다:

- Diary·Dream·Note = **Primary Content** — 세상(또는 주제)에 대한 기록.
- Reflection = **Commentary** — 기록에 대한 기록(해석 층).

Commentary 는 "글이 있음"이 아니라 **"A 에 대한 B"** 라는 관계로 정체가 정해진다. Reflection 은 Primary 의 서브타입이 아니라 별도 Aggregate Root(§2)다.

## 1. 존재론 (정본)

### 1.1 Primary vs Commentary
- **Primary Content** = Diary / Dream / Note. Primary 스트림(chapter·day·정렬)의 1급 멤버.
- **Commentary** = Reflection. Primary 에 매달리는 해석 층. 스트림의 peer 가 아니다.

### 1.2 About-A (필수 관계)
Commentary 의 정의상 Reflection 은 **반드시 어떤 기록(A)을 대상으로 한다.** 대상 참조는 **필수**다. 대상 없는 "독립 Reflection"은 정의 모순이며 존재하지 않는다(§1.4).
- 대상 A 는 Entry {DIARY, DREAM} 또는 다른 REFLECTION 이다. Reflection→Reflection(해석에 대한 해석, 재귀 About-A)이 성립한다.
- 예(추상): `Diary A ── interprets ──▶ Reflection B`, `Reflection B ── interprets ──▶ Reflection C`.

### 1.3 세 연산 (승격 삼분)
"승격"이라는 한 단어가 섞어온 세 연산을 분리한다:
1. **Reclassification(교정)** — 대상 없이 Reflection 으로 잘못 분류된 것은 사실 Diary/Note 다. 제 타입으로 되돌린다. *정체성: 애초에 Reflection 이 아니었다.*
2. **Generation(생성)** — 하나의 Reflection 이 **새 Primary(예: Note)를 낳는다.** 두 개의 별개 객체이며, 필요 시 출처 연결(provenance)만 남긴다. *정체성: 새 객체가 태어난다.*
3. **Promotion(전이)** — 같은 Reflection 객체가 Primary 로 바뀐다. **존재하지 않는다.**

Reflection 은 Primary 로 전이하지 않는다(3 없음). 사유가 자라 큰 글이 되는 것은 Generation(2)이지 Promotion 이 아니다.

### 1.4 Standalone Reflection 폐기
About-A(§1.2)의 귀결로, 대상 없는 Reflection 은 카테고리가 아니라 오분류다. 전량 **Reclassification(§1.3-1)** 으로 Diary/Note 로 착지시키고, 이후 생성은 대상 참조 필수로 막는다.

## 2. Aggregate

### 2.1 About-A 는 Aggregate Root 를 함의하지 않는다
"어떤 대상을 가리킨다"는 사실만으로는 독립 root 가 되지 않는다. `OrderLine` 은 `Order` 를 대상으로 하지만 `Order` aggregate 에 속한 내부 엔티티이지 별도 root 가 아니다. 즉 §1.2 의 About-A 는 Reflection 이 Primary 에 관계로 종속됨을 말할 뿐, 그것만으로 별도 root 를 세우지 않는다. 별도 root 여부는 관계가 아니라 **독립성**으로 가린다.

### 2.2 Reflection 이 독립 Aggregate Root 인 근거 — 독립성
Reflection 은 대상 aggregate 에 **속하지 않는다**. 근거는 세 독립성이다:
- **독립 수정**: 대상을 건드리지 않고 Reflection 만 수정한다. 대상의 일관성 경계 안에서 함께 바뀌지 않는다.
- **독립 생명주기**: 대상과 별개로 생성·삭제되며, 대상 작성 후 수년 뒤에도 덧붙는다. 대상 Primary 는 자기 불변을 위해 자기에 달린 Reflection 을 알 필요가 없고, Reflection 이 0개여도 완결이다.
- **독립 버전/감사**: 자기 audit·soft-delete·수정 이력을 대상과 분리해 갖는다.

이 셋이 성립하므로 Reflection 은 `OrderLine`(대상 aggregate 내부 엔티티)과 달리, 대상을 **id 로 참조**하는 별도 root 다. root 불변은 **대상 참조 필수**(About-A)이며, 이는 root 의 조건이 아니라 root 가 지키는 불변이다.

### 2.3 Entry 는 supertype 이지 소유 aggregate 가 아니다
Diary/Dream/Note = Primary Aggregate root 들이 **supertype(공유 record shape)** 을 공유한다. `Entry` 는 이 supertype 이지 Reflection 을 소유하는 aggregate 가 아니다.

## 3. 설계 원칙 (재사용): About-A 는 Root 를 함의하지 않는다

"관계는 root 를 정하지 않는다. **독립성**이 정한다." 이 원칙은 Reflection 밖에서도 성립한다:

- `Comment`(About-Article) · `Reply`(About-Comment) · `Annotation`(About-Doc): 대상을 가리키지만 독립 수정·생명주기·버전을 가지면 별도 root.
- `OrderLine`(part-of-Order): 대상을 가리키지만 Order 의 일관성 경계에 묶이면 내부 엔티티.

새 관계형 개념을 모델링할 때 "About-A 니까 root/내부"를 자동 결론짓지 말고 독립성 세 축(수정·생명주기·버전)으로 판정한다.

## 4. 결정 로그 (도메인 근거)

- **Reflection = Commentary(별도 AR)**: About-A 는 Commentary 종속을 뜻할 뿐 AR 을 함의하지 않는다(§2.1). 별도 AR 근거는 독립 수정·생명주기·버전(§2.2)이다.
- **Standalone 폐기·Promotion 없음**: About-A 의 귀결. 대상 없는 Reflection 은 오분류(교정), 큰 사유는 새 Primary 생성(Generation)이지 전이가 아니다.
- **Entry = supertype**: Diary/Dream/Note 의 공유 record shape 이지 Reflection 을 소유하는 aggregate 가 아니다.

## 5. 도메인 경계

- **대상 삭제 정책**: Reflection이 참조하는 대상의 삭제는 **Block(재귀)** 한다. 현재 런타임은 대상 엔트리·부모 Reflection·대상 엔트리를 포함한 챕터 삭제를 서비스 경계에서 거부한다. 여러 도메인의 삭제·복원·이동·복제·병합을 관계 타입 하나로 일반화하는 논의는 [관계 생명주기 의미론 아이디어](../../ideas/relationship-lifecycle-semantics.md)이며 이 계약의 정본이 아니다.
- **standalone 4행 → Note 재분류(확정)**: orphan-NOTE 의 대상 없는 Reflection 은 About-A 대상이 없어 애초에 Reflection 이 아니므로 `contentType` 을 JOURNAL_NOTE 로 교정(journal_entry 잔류).
