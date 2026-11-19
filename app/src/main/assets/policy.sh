# Shared pure decision function: never reads or writes hardware.
decide() {
 desired=0
 case "$mode" in observe|auto|manual|threshold) ;; *) return 1;; esac
 for value in "$capacity" "$online" "$temp" "$low" "$high"; do
  case "$value" in ''|*[!0-9]*) return 1;; esac
 done
 [ "$low" -ge 1 ] && [ "$low" -le 75 ] && [ "$high" -ge 76 ] && [ "$high" -le 100 ] || return 1
 [ "$capacity" -le 100 ] && [ "$online" -le 1 ] || return 1
 if [ "$mode:$low:$high" != "$previous" ]; then phase=charge; fi
 previous="$mode:$low:$high"
 if [ "$online" = 0 ] || [ "$temp" -ge 450 ] || [ "$mode" = observe ]; then
  phase=charge
  [ "$mode" = manual ] && mode=observe
 elif [ "$mode" = threshold ]; then
  if [ "$capacity" -le "$low" ]; then phase=charge
  elif [ "$capacity" -ge "$high" ]; then phase=bypass
  fi
  [ "$phase" = bypass ] && desired=1
 elif [ "$capacity" -ge 25 ]; then
  desired=1
 else
  [ "$mode" = manual ] && mode=observe
 fi
 return 0
}
