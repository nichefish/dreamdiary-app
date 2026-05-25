import type { NavigationProp, ParamListBase } from "@react-navigation/native";
import { StackActions } from "@react-navigation/native";

/**
 * EntryEdit 저장 후 목록으로 복귀.
 * EntryDetail 경유 시 2단 pop, 그 외(단독 push 등)는 1단 pop.
 */
export function exitAfterEntrySave(navigation: NavigationProp<ParamListBase>): void {
  const state = navigation.getState();
  const index = state.index;
  const prevRoute = index > 0 ? state.routes[index - 1] : undefined;
  const popCount = prevRoute?.name === "EntryDetail" ? 2 : 1;

  if (index >= popCount) {
    navigation.dispatch(StackActions.pop(popCount));
    return;
  }
  if (navigation.canGoBack()) {
    navigation.goBack();
  }
}
